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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
class AdminPedidoIntegracaoTest {

    private static final String CONSOLE = "console-portatil-r36";

    @Autowired
    private WebApplicationContext contexto;

    private MockMvc mockMvc;
    private String admin;
    private String cliente;
    private String emailCliente;
    private int pedidoId;
    private int estoqueAntes;

    @BeforeEach
    void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(contexto).apply(springSecurity()).build();
        admin = token(mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"admin@inovoustore.local\",\"senha\":\"admin12345\"}")));
        emailCliente = "admin-pedido-%s@teste.com".formatted(UUID.randomUUID());
        cliente = token(mockMvc.perform(post("/api/auth/cadastro").contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"Joana Cliente\",\"email\":\"%s\",\"senha\":\"senhaForte1\"}".formatted(emailCliente))));

        String console = mockMvc.perform(get("/api/produtos/" + CONSOLE)).andReturn().getResponse().getContentAsString();
        int consoleId = JsonPath.read(console, "$.id");
        estoqueAntes = JsonPath.read(console, "$.estoque");
        int enderecoId = JsonPath.read(mockMvc.perform(post("/api/me/enderecos").header("Authorization", cliente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"cep":"66010-000","logradouro":"Travessa Teste","numero":"10","complemento":null,
                                 "bairro":"Campina","cidade":"Belém","uf":"PA"}"""))
                .andReturn().getResponse().getContentAsString(), "$.id");
        mockMvc.perform(post("/api/carrinho/itens").header("Authorization", cliente)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"produtoId\":%d,\"quantidade\":2}".formatted(consoleId)));
        pedidoId = JsonPath.read(mockMvc.perform(post("/api/pedidos").header("Authorization", cliente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"enderecoId\":%d,\"formaPagamento\":\"NA_ENTREGA\"}".formatted(enderecoId)))
                .andReturn().getResponse().getContentAsString(), "$.id");
    }

    private String token(ResultActions resposta) throws Exception {
        return "Bearer " + JsonPath.read(resposta.andReturn().getResponse().getContentAsString(), "$.token");
    }

    private ResultActions mudarPara(String status) throws Exception {
        return mockMvc.perform(patch("/api/admin/pedidos/" + pedidoId + "/status").header("Authorization", admin)
                .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"%s\"}".formatted(status)));
    }

    private int estoqueDoConsole() throws Exception {
        return JsonPath.read(mockMvc.perform(get("/api/produtos/" + CONSOLE))
                .andReturn().getResponse().getContentAsString(), "$.estoque");
    }

    @Test
    void clienteNaoAcessaOsPedidosDoAdmin() throws Exception {
        mockMvc.perform(get("/api/admin/pedidos").header("Authorization", cliente)).andExpect(status().isForbidden());
    }

    @Test
    void encontraOPedidoPeloEmailECodigoComOsDadosDoCliente() throws Exception {
        mockMvc.perform(get("/api/admin/pedidos").header("Authorization", admin).param("busca", emailCliente))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalItens").value(1))
                .andExpect(jsonPath("$.itens[0].id").value(pedidoId))
                .andExpect(jsonPath("$.itens[0].cliente.nome").value("Joana Cliente"))
                .andExpect(jsonPath("$.itens[0].formaPagamento").value("NA_ENTREGA"))
                .andExpect(jsonPath("$.itens[0].quantidadeItens").value(2));

        mockMvc.perform(get("/api/admin/pedidos").header("Authorization", admin)
                        .param("busca", emailCliente).param("status", "ENTREGUE"))
                .andExpect(jsonPath("$.totalItens").value(0));

        mockMvc.perform(get("/api/admin/pedidos/" + pedidoId).header("Authorization", admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cliente.email").value(emailCliente))
                .andExpect(jsonPath("$.endereco.bairro").value("Campina"))
                .andExpect(jsonPath("$.proximosStatus[0]").value("PAGO"))
                .andExpect(jsonPath("$.proximosStatus[1]").value("CANCELADO"));
    }

    @Test
    void levaOPedidoAteEntregue() throws Exception {
        mudarPara("PAGO").andExpect(status().isOk()).andExpect(jsonPath("$.status").value("PAGO"));
        mudarPara("EM_SEPARACAO").andExpect(status().isOk());
        mudarPara("SAIU_PARA_ENTREGA").andExpect(status().isOk());
        mudarPara("ENTREGUE").andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ENTREGUE"))
                .andExpect(jsonPath("$.proximosStatus").isEmpty());

        mudarPara("CANCELADO").andExpect(status().isConflict());
        assertThat(estoqueDoConsole()).isEqualTo(estoqueAntes - 2);
    }

    @Test
    void naoPulaEtapasECancelarDevolveOEstoque() throws Exception {
        mudarPara("ENTREGUE").andExpect(status().isConflict());

        mudarPara("PAGO");
        mudarPara("EM_SEPARACAO");
        mudarPara("CANCELADO").andExpect(status().isOk()).andExpect(jsonPath("$.status").value("CANCELADO"));

        assertThat(estoqueDoConsole()).isEqualTo(estoqueAntes);
    }

    @Test
    void statusInvalidoOuPedidoInexistente() throws Exception {
        mudarPara("VOANDO").andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/admin/pedidos/999999").header("Authorization", admin))
                .andExpect(status().isNotFound());
    }
}
