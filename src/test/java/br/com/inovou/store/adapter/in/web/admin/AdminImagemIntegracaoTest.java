package br.com.inovou.store.adapter.in.web.admin;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.startsWith;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "inovou.uploads.diretorio=target/test-uploads")
@Transactional
class AdminImagemIntegracaoTest {

    private static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0, 0, 0, 0x0D};

    @Autowired
    private WebApplicationContext contexto;

    private MockMvc mockMvc;
    private String admin;
    private int produtoId;
    private String slug;

    @BeforeEach
    void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(contexto).apply(springSecurity()).build();
        admin = "Bearer " + JsonPath.read(mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"admin@inovoustore.local\",\"senha\":\"admin12345\"}"))
                .andReturn().getResponse().getContentAsString(), "$.token");
        int categoriaId = JsonPath.read(mockMvc.perform(get("/api/categorias"))
                .andReturn().getResponse().getContentAsString(), "$[0].id");
        String produto = mockMvc.perform(post("/api/admin/produtos").header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Produto Foto %s","descricao":"","preco":10.00,"estoque":2,"categoriaId":%d,"ativo":true}"""
                                .formatted(UUID.randomUUID().toString().substring(0, 8), categoriaId)))
                .andReturn().getResponse().getContentAsString();
        produtoId = JsonPath.read(produto, "$.id");
        slug = JsonPath.read(produto, "$.slug");
    }

    private ResultActions enviar(String nome, byte[] bytes) throws Exception {
        MockMultipartFile arquivo = new MockMultipartFile("arquivo", nome, "image/png", bytes);
        return mockMvc.perform(multipart("/api/admin/produtos/" + produtoId + "/imagens").file(arquivo)
                .header("Authorization", admin));
    }

    @Test
    void enviaReordenaERemoveFotos() throws Exception {
        String primeira = enviar("frente.png", PNG)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.imagens.length()").value(1))
                .andExpect(jsonPath("$.imagens[0].url", startsWith("/uploads/produtos/" + produtoId + "/")))
                .andExpect(jsonPath("$.imagens[0].ordem").value(0))
                .andReturn().getResponse().getContentAsString();
        String urlPrimeira = JsonPath.read(primeira, "$.imagens[0].url");

        mockMvc.perform(get(urlPrimeira)).andExpect(status().isOk()).andExpect(content().bytes(PNG));

        String duas = enviar("verso.png", PNG).andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        List<Integer> ids = JsonPath.read(duas, "$.imagens[*].id");

        mockMvc.perform(put("/api/admin/produtos/" + produtoId + "/imagens/ordem").header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ids\":[%d,%d]}".formatted(ids.get(1), ids.get(0))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imagens[0].id").value(ids.get(1)))
                .andExpect(jsonPath("$.imagens[1].id").value(ids.get(0)));

        mockMvc.perform(get("/api/produtos/" + slug))
                .andExpect(jsonPath("$.imagens[1]").value(urlPrimeira));

        mockMvc.perform(delete("/api/admin/produtos/" + produtoId + "/imagens/" + ids.get(0))
                        .header("Authorization", admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imagens.length()").value(1))
                .andExpect(jsonPath("$.imagens[0].ordem").value(0));

        mockMvc.perform(get(urlPrimeira)).andExpect(status().isNotFound());
    }

    @Test
    void recusaArquivoQueNaoEhImagem() throws Exception {
        enviar("nota.png", "%PDF-1.7".getBytes(StandardCharsets.US_ASCII))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("Envie uma foto em JPG, PNG ou WEBP"));
    }

    @Test
    void reordenarComListaIncompletaResponde400() throws Exception {
        enviar("a.png", PNG);
        enviar("b.png", PNG);
        int qualquer = JsonPath.read(mockMvc.perform(get("/api/admin/produtos/" + produtoId)
                .header("Authorization", admin)).andReturn().getResponse().getContentAsString(), "$.imagens[0].id");

        mockMvc.perform(put("/api/admin/produtos/" + produtoId + "/imagens/ordem").header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"ids\":[%d]}".formatted(qualquer)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void fotosSaoPublicasMasEnviarExigeAdmin() throws Exception {
        MockMultipartFile arquivo = new MockMultipartFile("arquivo", "a.png", "image/png", PNG);
        mockMvc.perform(multipart("/api/admin/produtos/" + produtoId + "/imagens").file(arquivo))
                .andExpect(status().isUnauthorized());
    }
}
