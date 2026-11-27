package br.com.inovou.store.application.consulta;

import br.com.inovou.store.domain.avaliacao.Avaliacao;

public record AvaliacaoComProduto(Avaliacao avaliacao, String produtoNome, String produtoSlug) {
}
