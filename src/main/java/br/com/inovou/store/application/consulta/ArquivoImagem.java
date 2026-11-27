package br.com.inovou.store.application.consulta;

import java.io.InputStream;

public record ArquivoImagem(String nomeOriginal, String tipoConteudo, long tamanho, InputStream conteudo) {
}
