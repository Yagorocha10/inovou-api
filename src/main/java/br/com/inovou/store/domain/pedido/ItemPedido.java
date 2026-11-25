package br.com.inovou.store.domain.pedido;

import br.com.inovou.store.domain.carrinho.ItemCarrinho;
import br.com.inovou.store.domain.exception.DomainException;
import br.com.inovou.store.domain.produto.ImagemProduto;
import br.com.inovou.store.domain.produto.Produto;

import java.math.BigDecimal;

public record ItemPedido(Long produtoId, String nome, String slug, String imagem,
                         BigDecimal precoUnitario, int quantidade) {

    public ItemPedido {
        if (produtoId == null) {
            throw new DomainException("O item do pedido precisa de um produto");
        }
        if (precoUnitario == null || precoUnitario.signum() <= 0) {
            throw new DomainException("O preço do item deve ser maior que zero");
        }
        if (quantidade <= 0) {
            throw new DomainException("A quantidade do item deve ser maior que zero");
        }
    }

    public static ItemPedido de(ItemCarrinho item) {
        Produto produto = item.produto();
        return new ItemPedido(produto.getId(), produto.getNome(), produto.getSlug(),
                produto.imagemPrincipal().map(ImagemProduto::url).orElse(null),
                produto.getPreco(), item.quantidade());
    }

    public BigDecimal subtotal() {
        return precoUnitario.multiply(BigDecimal.valueOf(quantidade));
    }
}
