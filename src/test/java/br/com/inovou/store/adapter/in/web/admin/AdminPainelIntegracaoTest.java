package br.com.inovou.store.adapter.in.web.admin;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
class AdminPainelIntegracaoTest {

    @Autowired
    private WebApplicationContext contexto;

    private MockMvc mockMvc;
    private String admin;

    private record Resumo(long pedidosHoje, BigDecimal faturamento, long paraPreparar, long semEstoque,
                          Integer primeiroPedido) {
    }

    @BeforeEach
    void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(contexto).apply(springSecurity()).build();
        admin = "Bearer " + JsonPath.read(mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"admin@inovoustore.local\",\"senha\":\"admin12345\"}"))
                .andReturn().getResponse().getContentAsString(), "$.token");
    }

    private Resumo resumo() throws Exception {
        String json = mockMvc.perform(get("/api/admin/resumo").header("Authorization", admin))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        Number pedidos = JsonPath.read(json, "$.pedidosHoje");
        Number faturamento = JsonPath.read(json, "$.faturamentoHoje");
        Number preparar = JsonPath.read(json, "$.pedidosParaPreparar");
        Number semEstoque = JsonPath.read(json, "$.produtosSemEstoque");
        Integer primeiro = JsonPath.<Integer>read(json, "$.ultimosPedidos.length()") == 0
                ? null : JsonPath.read(json, "$.ultimosPedidos[0].id");
        return new Resumo(pedidos.longValue(), new BigDecimal(faturamento.toString()), preparar.longValue(),
                semEstoque.longValue(), primeiro);
    }

    private int comprarDoisConsoles() throws Exception {
        String cliente = "Bearer " + JsonPath.read(mockMvc.perform(post("/api/auth/cadastro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Cliente Painel\",\"email\":\"painel-%s@teste.com\",\"senha\":\"senhaForte1\"}"
                                .formatted(UUID.randomUUID())))
                .andReturn().getResponse().getContentAsString(), "$.token");
        int consoleId = JsonPath.read(mockMvc.perform(get("/api/produtos/console-portatil-r36"))
                .andReturn().getResponse().getContentAsString(), "$.id");
        int enderecoId = JsonPath.read(mockMvc.perform(post("/api/me/enderecos").header("Authorization", cliente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"cep":"66010-000","logradouro":"Rua Painel","numero":"1","complemento":null,
                                 "bairro":"Campina","cidade":"Belém","uf":"PA"}"""))
                .andReturn().getResponse().getContentAsString(), "$.id");
        mockMvc.perform(post("/api/carrinho/itens").header("Authorization", cliente)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"produtoId\":%d,\"quantidade\":2}".formatted(consoleId)));
        String pedido = mockMvc.perform(post("/api/pedidos").header("Authorization", cliente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"enderecoId\":%d,\"formaPagamento\":\"PIX\"}".formatted(enderecoId)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return JsonPath.read(pedido, "$.id");
    }

    private void mudarPara(int pedidoId, String status) throws Exception {
        mockMvc.perform(patch("/api/admin/pedidos/" + pedidoId + "/status").header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"%s\"}".formatted(status)))
                .andExpect(status().isOk());
    }

    @Test
    void resumoAcompanhaOsPedidosDoDia() throws Exception {
        Resumo antes = resumo();
        String total = JsonPath.read(mockMvc.perform(get("/api/admin/pedidos/" + comprarDoisConsoles())
                .header("Authorization", admin)).andReturn().getResponse().getContentAsString(), "$.total").toString();
        Resumo depoisDaCompra = resumo();

        assertThat(depoisDaCompra.pedidosHoje()).isEqualTo(antes.pedidosHoje() + 1);
        assertThat(depoisDaCompra.faturamento()).isEqualByComparingTo(antes.faturamento().add(new BigDecimal(total)));
        assertThat(depoisDaCompra.paraPreparar()).isEqualTo(antes.paraPreparar());
        assertThat(depoisDaCompra.primeiroPedido()).isNotNull();

        mudarPara(depoisDaCompra.primeiroPedido(), "PAGO");
        assertThat(resumo().paraPreparar()).isEqualTo(antes.paraPreparar() + 1);

        mudarPara(depoisDaCompra.primeiroPedido(), "CANCELADO");
        Resumo depoisDoCancelamento = resumo();
        assertThat(depoisDoCancelamento.faturamento()).isEqualByComparingTo(antes.faturamento());
        assertThat(depoisDoCancelamento.pedidosHoje()).isEqualTo(antes.pedidosHoje() + 1);
        assertThat(depoisDoCancelamento.paraPreparar()).isEqualTo(antes.paraPreparar());
    }

    @Test
    void contaProdutosAtivosEsgotados() throws Exception {
        long antes = resumo().semEstoque();
        int categoriaId = JsonPath.read(mockMvc.perform(get("/api/categorias"))
                .andReturn().getResponse().getContentAsString(), "$[0].id");

        mockMvc.perform(post("/api/admin/produtos").header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Esgotado %s","descricao":"","preco":5.00,"estoque":0,"categoriaId":%d,"ativo":true}"""
                                .formatted(UUID.randomUUID().toString().substring(0, 8), categoriaId)))
                .andExpect(status().isCreated());

        assertThat(resumo().semEstoque()).isEqualTo(antes + 1);
    }

    @Test
    void resumoEhSoDoAdmin() throws Exception {
        mockMvc.perform(get("/api/admin/resumo")).andExpect(status().isUnauthorized());
    }
}
