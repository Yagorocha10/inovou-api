package br.com.inovou.store.adapter.in.web.pedido;

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
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
class PedidoIntegracaoTest {

    private static final String CONSOLE = "console-portatil-r36";

    @Autowired
    private WebApplicationContext contexto;

    private MockMvc mockMvc;
    private String token;
    private int consoleId;
    private int estoqueInicial;

    @BeforeEach
    void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(contexto).apply(springSecurity()).build();
        token = novoCliente();
        String console = mockMvc.perform(get("/api/produtos/" + CONSOLE)).andReturn().getResponse().getContentAsString();
        consoleId = JsonPath.read(console, "$.id");
        estoqueInicial = JsonPath.read(console, "$.estoque");
    }

    private String novoCliente() throws Exception {
        String sessao = mockMvc.perform(post("/api/auth/cadastro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Cliente Pedido\",\"email\":\"pedido-%s@teste.com\",\"senha\":\"senhaForte1\"}"
                                .formatted(UUID.randomUUID())))
                .andReturn().getResponse().getContentAsString();
        return "Bearer " + JsonPath.read(sessao, "$.token");
    }

    private int endereco(String cep, String cidade) throws Exception {
        String resposta = mockMvc.perform(post("/api/me/enderecos").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"cep":"%s","logradouro":"Travessa Teste","numero":"100","complemento":null,
                                 "bairro":"Centro","cidade":"%s","uf":"PA"}""".formatted(cep, cidade)))
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(resposta, "$.id");
    }

    private void adicionarConsole(int quantidade) throws Exception {
        mockMvc.perform(post("/api/carrinho/itens").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"produtoId\":%d,\"quantidade\":%d}".formatted(consoleId, quantidade)))
                .andExpect(status().isOk());
    }

    private ResultActions finalizar(int enderecoId, String forma) throws Exception {
        return mockMvc.perform(post("/api/pedidos").header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"enderecoId\":%d,\"formaPagamento\":\"%s\"}".formatted(enderecoId, forma)));
    }

    private int estoqueDoConsole() throws Exception {
        return JsonPath.read(mockMvc.perform(get("/api/produtos/" + CONSOLE))
                .andReturn().getResponse().getContentAsString(), "$.estoque");
    }

    @Test
    void pedidosExigemLogin() throws Exception {
        mockMvc.perform(get("/api/pedidos")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/checkout/resumo").param("enderecoId", "1")).andExpect(status().isUnauthorized());
    }

    @Test
    void compraCompletaComPixECancelamento() throws Exception {
        int enderecoId = endereco("66010-000", "Belém");
        adicionarConsole(2);

        mockMvc.perform(get("/api/checkout/resumo").header("Authorization", token)
                        .param("enderecoId", String.valueOf(enderecoId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itens.length()").value(1))
                .andExpect(jsonPath("$.subtotal").value(80.0))
                .andExpect(jsonPath("$.frete").value(10.0))
                .andExpect(jsonPath("$.total").value(90.0))
                .andExpect(jsonPath("$.descontoPix").value(4.0))
                .andExpect(jsonPath("$.totalPix").value(86.0))
                .andExpect(jsonPath("$.prazoEntregaDias").value(2))
                .andExpect(jsonPath("$.endereco.cidade").value("Belém"));

        String criado = finalizar(enderecoId, "PIX")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigo", startsWith("INV-")))
                .andExpect(jsonPath("$.status").value("AGUARDANDO_PAGAMENTO"))
                .andExpect(jsonPath("$.desconto").value(4.0))
                .andExpect(jsonPath("$.total").value(86.0))
                .andExpect(jsonPath("$.itens[0].precoUnitario").value(40.0))
                .andExpect(jsonPath("$.pagamento.forma").value("PIX"))
                .andExpect(jsonPath("$.pagamento.codigoPix").isNotEmpty())
                .andExpect(jsonPath("$.podeCancelar").value(true))
                .andReturn().getResponse().getContentAsString();
        int pedidoId = JsonPath.read(criado, "$.id");

        assertThat(estoqueDoConsole()).isEqualTo(estoqueInicial - 2);
        mockMvc.perform(get("/api/carrinho").header("Authorization", token))
                .andExpect(jsonPath("$.quantidadeTotal").value(0));

        mockMvc.perform(get("/api/pedidos").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalItens").value(1))
                .andExpect(jsonPath("$.itens[0].id").value(pedidoId))
                .andExpect(jsonPath("$.itens[0].quantidadeItens").value(2));

        mockMvc.perform(post("/api/pedidos/" + pedidoId + "/cancelar").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELADO"))
                .andExpect(jsonPath("$.podeCancelar").value(false));
        assertThat(estoqueDoConsole()).isEqualTo(estoqueInicial);

        mockMvc.perform(post("/api/pedidos/" + pedidoId + "/cancelar").header("Authorization", token))
                .andExpect(status().isConflict());
    }

    @Test
    void cartaoJaNascePago() throws Exception {
        int enderecoId = endereco("67030-000", "Ananindeua");
        adicionarConsole(1);

        finalizar(enderecoId, "CARTAO")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PAGO"))
                .andExpect(jsonPath("$.frete").value(15.0))
                .andExpect(jsonPath("$.pagamento.codigoPix").doesNotExist());
    }

    @Test
    void cepForaDaAreaDeEntregaResponde400() throws Exception {
        int enderecoId = endereco("01310-100", "São Paulo");
        adicionarConsole(1);

        mockMvc.perform(get("/api/checkout/resumo").header("Authorization", token)
                        .param("enderecoId", String.valueOf(enderecoId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value(startsWith("Ainda não entregamos no CEP 01310-100")));
        finalizar(enderecoId, "PIX").andExpect(status().isBadRequest());
        assertThat(estoqueDoConsole()).isEqualTo(estoqueInicial);
    }

    @Test
    void carrinhoVazioEFormaDePagamentoInvalidaRespondem400() throws Exception {
        int enderecoId = endereco("66010-000", "Belém");

        finalizar(enderecoId, "PIX")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("Seu carrinho está vazio"));

        adicionarConsole(1);
        finalizar(enderecoId, "BOLETO").andExpect(status().isBadRequest());
    }

    @Test
    void pedidoDeOutroClienteResponde404() throws Exception {
        int enderecoId = endereco("66010-000", "Belém");
        adicionarConsole(1);
        int pedidoId = JsonPath.read(finalizar(enderecoId, "NA_ENTREGA")
                .andReturn().getResponse().getContentAsString(), "$.id");

        String outroCliente = novoCliente();
        mockMvc.perform(get("/api/pedidos/" + pedidoId).header("Authorization", outroCliente))
                .andExpect(status().isNotFound());
        mockMvc.perform(post("/api/pedidos/" + pedidoId + "/cancelar").header("Authorization", outroCliente))
                .andExpect(status().isNotFound());
    }
}
