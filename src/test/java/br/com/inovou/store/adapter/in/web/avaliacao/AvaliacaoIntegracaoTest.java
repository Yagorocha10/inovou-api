package br.com.inovou.store.adapter.in.web.avaliacao;

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

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
class AvaliacaoIntegracaoTest {

    private static final String CONSOLE = "console-portatil-r36";

    @Autowired
    private WebApplicationContext contexto;

    private MockMvc mockMvc;
    private String admin;
    private String cliente;

    @BeforeEach
    void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(contexto).apply(springSecurity()).build();
        admin = token(mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"admin@inovoustore.local\",\"senha\":\"admin12345\"}")));
        cliente = token(mockMvc.perform(post("/api/auth/cadastro").contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"Mariana Souza\",\"email\":\"avaliacao-%s@teste.com\",\"senha\":\"senhaForte1\"}"
                        .formatted(UUID.randomUUID()))));
    }

    private String token(ResultActions resposta) throws Exception {
        return "Bearer " + JsonPath.read(resposta.andReturn().getResponse().getContentAsString(), "$.token");
    }

    private int comprarUmConsole() throws Exception {
        int consoleId = JsonPath.read(mockMvc.perform(get("/api/produtos/" + CONSOLE))
                .andReturn().getResponse().getContentAsString(), "$.id");
        int enderecoId = JsonPath.read(mockMvc.perform(post("/api/me/enderecos").header("Authorization", cliente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"cep":"66010-000","logradouro":"Travessa Teste","numero":"10","complemento":null,
                                 "bairro":"Campina","cidade":"Belém","uf":"PA"}"""))
                .andReturn().getResponse().getContentAsString(), "$.id");
        mockMvc.perform(post("/api/carrinho/itens").header("Authorization", cliente)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"produtoId\":%d,\"quantidade\":1}".formatted(consoleId)));
        return JsonPath.read(mockMvc.perform(post("/api/pedidos").header("Authorization", cliente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"enderecoId\":%d,\"formaPagamento\":\"NA_ENTREGA\"}".formatted(enderecoId)))
                .andReturn().getResponse().getContentAsString(), "$.id");
    }

    private void entregar(int pedidoId) throws Exception {
        for (String status : new String[]{"PAGO", "EM_SEPARACAO", "SAIU_PARA_ENTREGA", "ENTREGUE"}) {
            mockMvc.perform(patch("/api/admin/pedidos/" + pedidoId + "/status").header("Authorization", admin)
                            .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"%s\"}".formatted(status)))
                    .andExpect(status().isOk());
        }
    }

    private ResultActions avaliar(String token, String corpo) throws Exception {
        return mockMvc.perform(post("/api/produtos/" + CONSOLE + "/avaliacoes").header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON).content(corpo));
    }

    private ResultActions permissao() throws Exception {
        return mockMvc.perform(get("/api/produtos/" + CONSOLE + "/avaliacoes/permissao")
                .header("Authorization", cliente));
    }

    @Test
    void clienteAvaliaDepoisDeReceberEAAvaliacaoApareceNaLoja() throws Exception {
        permissao().andExpect(status().isOk()).andExpect(jsonPath("$.podeAvaliar").value(false));

        entregar(comprarUmConsole());
        permissao().andExpect(jsonPath("$.podeAvaliar").value(true));

        avaliar(cliente, "{\"nota\":5,\"comentario\":\"Chegou rapidinho, recomendo\"}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nota").value(5))
                .andExpect(jsonPath("$.autor").value("Mariana S."));

        permissao().andExpect(jsonPath("$.podeAvaliar").value(false))
                .andExpect(jsonPath("$.motivo", containsString("já avaliou")));
        avaliar(cliente, "{\"nota\":4}").andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/produtos/" + CONSOLE + "/avaliacoes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.avaliacoes.itens[0].autor").value("Mariana S."));
        mockMvc.perform(get("/api/produtos/" + CONSOLE))
                .andExpect(jsonPath("$.totalAvaliacoes", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.notaMedia").isNotEmpty());
        mockMvc.perform(get("/api/avaliacoes/recentes").param("limite", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].comentario").value("Chegou rapidinho, recomendo"))
                .andExpect(jsonPath("$[0].produtoSlug").value(CONSOLE));
        mockMvc.perform(get("/api/produtos/mais-vendidos").param("limite", "24"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].slug", hasItem(CONSOLE)));
    }

    @Test
    void semPedidoEntregueNaoDaParaAvaliar() throws Exception {
        comprarUmConsole();

        avaliar(cliente, "{\"nota\":5}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem", containsString("depois de recebê-lo")));
    }

    @Test
    void notaForaDeUmACincoEhRecusada() throws Exception {
        avaliar(cliente, "{\"nota\":6}").andExpect(status().isBadRequest());
        avaliar(cliente, "{\"comentario\":\"sem nota\"}").andExpect(status().isBadRequest());
    }

    @Test
    void visitanteVeAsAvaliacoesMasNaoAvalia() throws Exception {
        mockMvc.perform(get("/api/produtos/" + CONSOLE + "/avaliacoes/permissao"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.podeAvaliar").value(false))
                .andExpect(jsonPath("$.motivo", containsString("Entre na sua conta")));
        mockMvc.perform(post("/api/produtos/" + CONSOLE + "/avaliacoes")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"nota\":5}"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/produtos/" + CONSOLE + "/avaliacoes")).andExpect(status().isOk());
        mockMvc.perform(get("/api/avaliacoes/recentes")).andExpect(status().isOk());
    }
}
