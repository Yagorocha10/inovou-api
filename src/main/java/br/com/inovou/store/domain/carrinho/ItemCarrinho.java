package br.com.inovou.store.domain.carrinho;

import br.com.inovou.store.domain.exception.DomainException;
import br.com.inovou.store.domain.produto.Produto;

import java.math.BigDecimal;

public record ItemCarrinho(Produto produto, int quantidade) {

    public ItemCarrinho {
        if (produto == null) {
            throw new DomainException("O item do carrinho precisa de um produto");
        }
        if (quantidade <= 0) {
            throw new DomainException("A quantidade deve ser maior que zero");
        }
    }

    public Long produtoId() {
        return produto.getId();
    }

    public BigDecimal subtotal() {
        return produto.getPreco().multiply(BigDecimal.valueOf(quantidade));
    }

    public boolean podeSerComprado() {
        return produto.isAtivo() && produto.temEstoque(quantidade);
    }

    ItemCarrinho comQuantidade(int novaQuantidade) {
        return new ItemCarrinho(produto, novaQuantidade);
    }
}
