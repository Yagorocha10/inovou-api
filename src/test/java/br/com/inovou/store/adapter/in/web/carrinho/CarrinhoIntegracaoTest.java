package br.com.inovou.store.adapter.in.web.carrinho;

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

import java.util.UUID;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
class CarrinhoIntegracaoTest {

    @Autowired
    private WebApplicationContext contexto;

    private MockMvc mockMvc;
    private String token;
    private int consoleId;
    private int gameStickId;

    @BeforeEach
    void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(contexto).apply(springSecurity()).build();
        String sessao = mockMvc.perform(post("/api/auth/cadastro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Cliente Carrinho\",\"email\":\"carrinho-%s@teste.com\",\"senha\":\"senhaForte1\"}"
                                .formatted(UUID.randomUUID())))
                .andReturn().getResponse().getContentAsString();
        token = "Bearer " + JsonPath.read(sessao, "$.token");
        consoleId = JsonPath.read(mockMvc.perform(get("/api/produtos/console-portatil-r36"))
                .andReturn().getResponse().getContentAsString(), "$.id");
        gameStickId = JsonPath.read(mockMvc.perform(get("/api/produtos/game-stick"))
                .andReturn().getResponse().getContentAsString(), "$.id");
    }

    private String item(int produtoId, int quantidade) {
        return "{\"produtoId\":%d,\"quantidade\":%d}".formatted(produtoId, quantidade);
    }

    @Test
    void carrinhoExigeLogin() throws Exception {
        mockMvc.perform(get("/api/carrinho")).andExpect(status().isUnauthorized());
    }

    @Test
    void carrinhoNovoComecaVazio() throws Exception {
        mockMvc.perform(get("/api/carrinho").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itens").isEmpty())
                .andExpect(jsonPath("$.quantidadeTotal").value(0))
                .andExpect(jsonPath("$.podeFinalizar").value(false));
    }

    @Test
    void fluxoCompletoDoCarrinho() throws Exception {
        mockMvc.perform(post("/api/carrinho/itens").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON).content(item(consoleId, 2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantidadeTotal").value(2))
                .andExpect(jsonPath("$.itens[0].produtoId").value(consoleId))
                .andExpect(jsonPath("$.podeFinalizar").value(true));

        mockMvc.perform(post("/api/carrinho/itens").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON).content(item(consoleId, 2)))
                .andExpect(jsonPath("$.itens.length()").value(1))
                .andExpect(jsonPath("$.quantidadeTotal").value(4));

        mockMvc.perform(post("/api/carrinho/itens").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON).content(item(consoleId, 2)))
                .andExpect(status().isConflict());

        mockMvc.perform(put("/api/carrinho/itens/" + consoleId).header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"quantidade\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantidadeTotal").value(1));

        mockMvc.perform(get("/api/carrinho").header("Authorization", token))
                .andExpect(jsonPath("$.quantidadeTotal").value(1));

        mockMvc.perform(delete("/api/carrinho/itens/" + consoleId).header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itens").isEmpty());
    }

    @Test
    void produtoEsgotadoNaoEntraNoCarrinho() throws Exception {
        mockMvc.perform(post("/api/carrinho/itens").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON).content(item(gameStickId, 1)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("O produto Game Stick não está disponível no momento"));
    }

    @Test
    void quantidadeInvalidaRespondeCampoComErro() throws Exception {
        mockMvc.perform(post("/api/carrinho/itens").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON).content(item(consoleId, 0)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.quantidade").exists());
    }
}
