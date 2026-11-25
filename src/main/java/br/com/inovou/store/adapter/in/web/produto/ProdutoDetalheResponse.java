package br.com.inovou.store.adapter.in.web.produto;

import br.com.inovou.store.adapter.in.web.categoria.CategoriaResponse;
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
        int estoque,
        boolean disponivel,
        CategoriaResponse categoria,
        List<String> imagens) {

    public static ProdutoDetalheResponse de(Produto produto) {
        return new ProdutoDetalheResponse(
                produto.getId(),
                produto.getNome(),
                produto.getSlug(),
                produto.getDescricao(),
                produto.getPreco(),
                produto.getEstoque(),
                produto.disponivelParaVenda(),
                CategoriaResponse.de(produto.getCategoria()),
                produto.getImagens().stream().map(ImagemProduto::url).toList());
    }
}
