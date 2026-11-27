package br.com.inovou.store.adapter.out.armazenamento;

import br.com.inovou.store.application.consulta.ArquivoImagem;
import br.com.inovou.store.application.port.out.ArmazenamentoImagemPort;
import br.com.inovou.store.domain.exception.ArquivoInvalidoException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.stream.IntStream;

@Component
public class DiscoLocalArmazenamentoAdapter implements ArmazenamentoImagemPort {

    static final String PREFIXO_URL = "/uploads/";
    static final int TAMANHO_MAXIMO = 5 * 1024 * 1024;

    private final Path raiz;

    public DiscoLocalArmazenamentoAdapter(@Value("${inovou.uploads.diretorio}") String diretorio) {
        this.raiz = Path.of(diretorio).toAbsolutePath().normalize();
    }

    @Override
    public String salvar(Long produtoId, ArquivoImagem arquivo) {
        byte[] bytes = lerAteOLimite(arquivo.conteudo());
        String extensao = extensaoPeloConteudo(bytes);
        String relativo = "produtos/%d/%s.%s".formatted(produtoId, UUID.randomUUID(), extensao);
        Path destino = raiz.resolve(relativo);
        try {
            Files.createDirectories(destino.getParent());
            Files.write(destino, bytes);
        } catch (IOException falha) {
            throw new UncheckedIOException("Não foi possível gravar a foto", falha);
        }
        return PREFIXO_URL + relativo;
    }

    @Override
    public void remover(String url) {
        if (url == null || !url.startsWith(PREFIXO_URL)) {
            return;
        }
        Path alvo = raiz.resolve(url.substring(PREFIXO_URL.length())).normalize();
        if (!alvo.startsWith(raiz)) {
            throw new ArquivoInvalidoException("Caminho de foto inválido");
        }
        try {
            Files.deleteIfExists(alvo);
        } catch (IOException falha) {
            throw new UncheckedIOException("Não foi possível apagar a foto", falha);
        }
    }

    Path raiz() {
        return raiz;
    }

    private static byte[] lerAteOLimite(InputStream conteudo) {
        try (InputStream entrada = conteudo) {
            byte[] bytes = entrada.readNBytes(TAMANHO_MAXIMO + 1);
            if (bytes.length == 0) {
                throw new ArquivoInvalidoException("O arquivo enviado está vazio");
            }
            if (bytes.length > TAMANHO_MAXIMO) {
                throw new ArquivoInvalidoException("A foto deve ter no máximo 5 MB");
            }
            return bytes;
        } catch (IOException falha) {
            throw new UncheckedIOException("Não foi possível ler a foto enviada", falha);
        }
    }

    static String extensaoPeloConteudo(byte[] bytes) {
        if (comecaCom(bytes, 0, 0xFF, 0xD8, 0xFF)) {
            return "jpg";
        }
        if (comecaCom(bytes, 0, 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A)) {
            return "png";
        }
        if (comecaCom(bytes, 0, 'R', 'I', 'F', 'F') && comecaCom(bytes, 8, 'W', 'E', 'B', 'P')) {
            return "webp";
        }
        throw new ArquivoInvalidoException("Envie uma foto em JPG, PNG ou WEBP");
    }

    private static boolean comecaCom(byte[] bytes, int inicio, int... assinatura) {
        if (bytes.length < inicio + assinatura.length) {
            return false;
        }
        return IntStream.range(0, assinatura.length)
                .allMatch(i -> (bytes[inicio + i] & 0xFF) == assinatura[i]);
    }
}
