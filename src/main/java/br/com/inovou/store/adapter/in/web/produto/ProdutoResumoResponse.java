package br.com.inovou.store.adapter.in.web.produto;

import br.com.inovou.store.adapter.in.web.categoria.CategoriaResponse;
import br.com.inovou.store.domain.produto.ImagemProduto;
import br.com.inovou.store.domain.produto.Produto;

import java.math.BigDecimal;

public record ProdutoResumoResponse(
        Long id,
        String nome,
        String slug,
        BigDecimal preco,
        String imagemPrincipal,
        CategoriaResponse categoria,
        boolean disponivel) {

    public static ProdutoResumoResponse de(Produto produto) {
        return new ProdutoResumoResponse(
                produto.getId(),
                produto.getNome(),
                produto.getSlug(),
                produto.getPreco(),
                produto.imagemPrincipal().map(ImagemProduto::url).orElse(null),
                CategoriaResponse.de(produto.getCategoria()),
                produto.disponivelParaVenda());
    }
}
