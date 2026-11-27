package br.com.inovou.store.adapter.in.web.avaliacao;

import br.com.inovou.store.adapter.in.web.comum.PaginaResponse;
import br.com.inovou.store.application.consulta.AvaliacoesDoProduto;

import java.math.BigDecimal;

public record AvaliacoesProdutoResponse(BigDecimal media, long total, PaginaResponse<AvaliacaoResponse> avaliacoes) {

    static AvaliacoesProdutoResponse de(AvaliacoesDoProduto avaliacoes) {
        return new AvaliacoesProdutoResponse(avaliacoes.resumo().media(), avaliacoes.resumo().total(),
                PaginaResponse.de(avaliacoes.avaliacoes(), AvaliacaoResponse::de));
    }
}
