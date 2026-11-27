package br.com.inovou.store.adapter.in.web.admin;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.UUID;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
class AdminProdutoIntegracaoTest {

    @Autowired
    private WebApplicationContext contexto;

    private MockMvc mockMvc;
    private String admin;
    private int categoriaId;
    private String nome;

    @BeforeEach
    void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(contexto).apply(springSecurity()).build();
        String sessao = mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"admin@inovoustore.local\",\"senha\":\"admin12345\"}"))
                .andReturn().getResponse().getContentAsString();
        admin = "Bearer " + JsonPath.read(sessao, "$.token");
        categoriaId = JsonPath.read(mockMvc.perform(get("/api/categorias"))
                .andReturn().getResponse().getContentAsString(), "$[0].id");
        nome = "Fone Teste " + UUID.randomUUID().toString().substring(0, 8);
    }

    private String corpo(String nomeProduto, String preco, int estoque) {
        return """
                {"nome":"%s","descricao":"Fone sem fio","preco":%s,"estoque":%d,"categoriaId":%d,"ativo":true}"""
                .formatted(nomeProduto, preco, estoque, categoriaId);
    }

    private ResultActions criar(String corpo) throws Exception {
        return mockMvc.perform(post("/api/admin/produtos").header("Authorization", admin)
                .contentType(MediaType.APPLICATION_JSON).content(corpo));
    }

    @Test
    void soAdminAcessa() throws Exception {
        mockMvc.perform(get("/api/admin/produtos")).andExpect(status().isUnauthorized());

        String cliente = mockMvc.perform(post("/api/auth/cadastro").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Cliente\",\"email\":\"cli-%s@teste.com\",\"senha\":\"senhaForte1\"}"
                                .formatted(UUID.randomUUID())))
                .andReturn().getResponse().getContentAsString();
        mockMvc.perform(get("/api/admin/produtos").header("Authorization", "Bearer " + JsonPath.read(cliente, "$.token")))
                .andExpect(status().isForbidden());
    }

    @Test
    void criaEditaEDesativaProduto() throws Exception {
        String criado = criar(corpo(nome, "59.90", 3))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value(nome))
                .andExpect(jsonPath("$.slug").isNotEmpty())
                .andExpect(jsonPath("$.preco").value(59.9))
                .andExpect(jsonPath("$.ativo").value(true))
                .andExpect(jsonPath("$.imagens").isEmpty())
                .andReturn().getResponse().getContentAsString();
        int id = JsonPath.read(criado, "$.id");
        String slug = JsonPath.read(criado, "$.slug");

        mockMvc.perform(get("/api/produtos/" + slug)).andExpect(status().isOk());

        mockMvc.perform(put("/api/admin/produtos/" + id).header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON).content(corpo(nome + " Pro", "89.90", 0)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.preco").value(89.9))
                .andExpect(jsonPath("$.estoque").value(0))
                .andExpect(jsonPath("$.slug").value(slug));

        mockMvc.perform(patch("/api/admin/produtos/" + id + "/ativo").header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"ativo\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ativo").value(false));

        mockMvc.perform(get("/api/produtos/" + slug)).andExpect(status().isNotFound());

        mockMvc.perform(get("/api/admin/produtos").header("Authorization", admin)
                        .param("situacao", "INATIVOS").param("busca", nome))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalItens").value(1))
                .andExpect(jsonPath("$.itens[0].id").value(id));
    }

    @Test
    void nomeRepetidoResponde409NoCampoNome() throws Exception {
        criar(corpo(nome, "59.90", 3)).andExpect(status().isCreated());

        criar(corpo(nome.toUpperCase(), "10.00", 1))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.campos.nome").exists());
    }

    @Test
    void validaOsCamposDoFormulario() throws Exception {
        criar(corpo("", "59.999", -1))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.nome").exists())
                .andExpect(jsonPath("$.campos.preco").exists())
                .andExpect(jsonPath("$.campos.estoque").exists());
    }

    @Test
    void categoriaOuProdutoInexistenteResponde404() throws Exception {
        criar(corpo(nome, "59.90", 3).replace("\"categoriaId\":" + categoriaId, "\"categoriaId\":999999"))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/admin/produtos/999999").header("Authorization", admin))
                .andExpect(status().isNotFound());
    }

    @Test
    void precoPromocionalApareceNaVitrineENoFiltroDeOfertas() throws Exception {
        String criado = criar("""
                {"nome":"%s","descricao":"Fone sem fio","preco":149.90,"precoAnterior":199.90,"estoque":3,"categoriaId":%d,"ativo":true}"""
                .formatted(nome, categoriaId))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.precoAnterior").value(199.9))
                .andReturn().getResponse().getContentAsString();
        String slug = JsonPath.read(criado, "$.slug");

        mockMvc.perform(get("/api/produtos/" + slug))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.precoAnterior").value(199.9))
                .andExpect(jsonPath("$.percentualDesconto").value(25))
                .andExpect(jsonPath("$.precoPix").value(142.4));
        mockMvc.perform(get("/api/produtos").param("ofertas", "true").param("busca", nome))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalItens").value(1))
                .andExpect(jsonPath("$.itens[0].slug").value(slug));
    }

    @Test
    void precoAnteriorMenorQueOPrecoEhRecusado() throws Exception {
        criar("""
                {"nome":"%s","descricao":"","preco":50.00,"precoAnterior":40.00,"estoque":3,"categoriaId":%d,"ativo":true}"""
                .formatted(nome, categoriaId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("O preço anterior deve ser maior que o preço atual para aparecer como promoção"));
    }
}
