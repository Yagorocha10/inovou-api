package br.com.inovou.store.adapter.in.web.produto;

import br.com.inovou.store.adapter.in.web.categoria.CategoriaResponse;
import br.com.inovou.store.application.consulta.ResumoAvaliacoes;
import br.com.inovou.store.domain.pedido.DescontoPix;
import br.com.inovou.store.domain.produto.ImagemProduto;
import br.com.inovou.store.domain.produto.Produto;

import java.math.BigDecimal;
import java.util.List;

public record ProdutoDetalheResponse(
        Long id,
        String nome,
        String slug,
        String descricao,
        BigDecimal preco,
        BigDecimal precoAnterior,
        int percentualDesconto,
        BigDecimal precoPix,
        int estoque,
        boolean disponivel,
        CategoriaResponse categoria,
        List<String> imagens,
        BigDecimal notaMedia,
        long totalAvaliacoes) {

    public static ProdutoDetalheResponse de(Produto produto) {
        return de(produto, ResumoAvaliacoes.vazio());
    }

    public static ProdutoDetalheResponse de(Produto produto, ResumoAvaliacoes avaliacoes) {
        ResumoAvaliacoes resumo = avaliacoes == null ? ResumoAvaliacoes.vazio() : avaliacoes;
        return new ProdutoDetalheResponse(
                produto.getId(),
                produto.getNome(),
                produto.getSlug(),
                produto.getDescricao(),
                produto.getPreco(),
                produto.getPrecoAnterior(),
                produto.percentualDesconto(),
                DescontoPix.precoNoPix(produto.getPreco()),
                produto.getEstoque(),
                produto.disponivelParaVenda(),
                CategoriaResponse.de(produto.getCategoria()),
                produto.getImagens().stream().map(ImagemProduto::url).toList(),
                resumo.media(),
                resumo.total());
    }
}
