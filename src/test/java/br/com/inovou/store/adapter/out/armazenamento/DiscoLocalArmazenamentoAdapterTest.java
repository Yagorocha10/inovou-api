package br.com.inovou.store.adapter.out.armazenamento;

import br.com.inovou.store.application.consulta.ArquivoImagem;
import br.com.inovou.store.domain.exception.ArquivoInvalidoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DiscoLocalArmazenamentoAdapterTest {

    static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0, 0, 0, 0x0D};
    static final byte[] JPG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0, 0x10};
    static final byte[] WEBP = {'R', 'I', 'F', 'F', 0, 0, 0, 0, 'W', 'E', 'B', 'P', 'V', 'P', '8'};

    @TempDir
    Path pasta;

    private DiscoLocalArmazenamentoAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new DiscoLocalArmazenamentoAdapter(pasta.toString());
    }

    private ArquivoImagem arquivo(String nome, byte[] bytes) {
        return new ArquivoImagem(nome, "image/png", bytes.length, new ByteArrayInputStream(bytes));
    }

    @Test
    void salvaComNomeAleatorioEDevolveAUrlPublica() throws Exception {
        String url = adapter.salvar(12L, arquivo("../../foto do cliente.png", PNG));

        assertThat(url).startsWith("/uploads/produtos/12/").endsWith(".png").doesNotContain("cliente");
        Path gravado = pasta.resolve(url.substring("/uploads/".length()));
        assertThat(gravado).exists();
        assertThat(Files.readAllBytes(gravado)).isEqualTo(PNG);
    }

    @Test
    void descobreAExtensaoPeloConteudoENaoPeloNome() {
        assertThat(adapter.salvar(1L, arquivo("foto.png", JPG))).endsWith(".jpg");
        assertThat(adapter.salvar(1L, arquivo("foto.jpg", WEBP))).endsWith(".webp");
    }

    @Test
    void recusaArquivoQueNaoEhImagemMesmoComExtensaoDeImagem() {
        byte[] pdf = "%PDF-1.7 conteudo".getBytes(StandardCharsets.US_ASCII);

        assertThatThrownBy(() -> adapter.salvar(1L, arquivo("nota.jpg", pdf)))
                .isInstanceOf(ArquivoInvalidoException.class)
                .hasMessageContaining("JPG, PNG ou WEBP");
        assertThatThrownBy(() -> adapter.salvar(1L, arquivo("vazio.png", new byte[0])))
                .isInstanceOf(ArquivoInvalidoException.class);
    }

    @Test
    void removeOArquivoEIgnoraUrlsDeFora() {
        String url = adapter.salvar(3L, arquivo("a.png", PNG));
        Path gravado = pasta.resolve(url.substring("/uploads/".length()));

        adapter.remover(url);
        adapter.remover(url);
        adapter.remover("https://cdn.exemplo.com/foto.png");

        assertThat(gravado).doesNotExist();
    }

    @Test
    void naoApagaNadaForaDaPastaDeUploads() throws Exception {
        Path importante = Files.writeString(pasta.getParent().resolve("importante-" + System.nanoTime() + ".txt"), "x");

        assertThatThrownBy(() -> adapter.remover("/uploads/../" + importante.getFileName()))
                .isInstanceOf(ArquivoInvalidoException.class);
        assertThat(importante).exists();
        Files.deleteIfExists(importante);
    }
}
