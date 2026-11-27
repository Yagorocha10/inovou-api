package br.com.inovou.store.adapter.in.web.avaliacao;

import br.com.inovou.store.application.consulta.AvaliacaoComProduto;

import java.time.LocalDateTime;

public record DepoimentoResponse(int nota, String comentario, String autor, String produtoNome, String produtoSlug,
                                 LocalDateTime criadoEm) {

    static DepoimentoResponse de(AvaliacaoComProduto avaliacaoComProduto) {
        var avaliacao = avaliacaoComProduto.avaliacao();
        return new DepoimentoResponse(avaliacao.nota(), avaliacao.comentario(), avaliacao.autor(),
                avaliacaoComProduto.produtoNome(), avaliacaoComProduto.produtoSlug(), avaliacao.criadoEm());
    }
}
