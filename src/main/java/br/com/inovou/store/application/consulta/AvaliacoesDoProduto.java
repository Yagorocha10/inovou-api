package br.com.inovou.store.application.consulta;

import br.com.inovou.store.domain.avaliacao.Avaliacao;

public record AvaliacoesDoProduto(ResumoAvaliacoes resumo, Pagina<Avaliacao> avaliacoes) {
}
