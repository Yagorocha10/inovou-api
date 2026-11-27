package br.com.inovou.store.adapter.in.web.produto;

import br.com.inovou.store.adapter.in.web.categoria.CategoriaResponse;
import br.com.inovou.store.application.consulta.ResumoAvaliacoes;
import br.com.inovou.store.domain.pedido.DescontoPix;
import br.com.inovou.store.domain.produto.ImagemProduto;
import br.com.inovou.store.domain.produto.Produto;

import java.math.BigDecimal;

public record ProdutoResumoResponse(
        Long id,
        String nome,
        String slug,
        BigDecimal preco,
        BigDecimal precoAnterior,
        int percentualDesconto,
        BigDecimal precoPix,
        String imagemPrincipal,
        CategoriaResponse categoria,
        boolean disponivel,
        BigDecimal notaMedia,
        long totalAvaliacoes) {

    public static ProdutoResumoResponse de(Produto produto) {
        return de(produto, ResumoAvaliacoes.vazio());
    }

    public static ProdutoResumoResponse de(Produto produto, ResumoAvaliacoes avaliacoes) {
        ResumoAvaliacoes resumo = avaliacoes == null ? ResumoAvaliacoes.vazio() : avaliacoes;
        return new ProdutoResumoResponse(
                produto.getId(),
                produto.getNome(),
                produto.getSlug(),
                produto.getPreco(),
                produto.getPrecoAnterior(),
                produto.percentualDesconto(),
                DescontoPix.precoNoPix(produto.getPreco()),
                produto.imagemPrincipal().map(ImagemProduto::url).orElse(null),
                CategoriaResponse.de(produto.getCategoria()),
                produto.disponivelParaVenda(),
                resumo.media(),
                resumo.total());
    }
}
