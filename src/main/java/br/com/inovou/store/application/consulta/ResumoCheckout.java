package br.com.inovou.store.application.consulta;

import br.com.inovou.store.domain.frete.Frete;
import br.com.inovou.store.domain.pedido.DescontoPix;
import br.com.inovou.store.domain.pedido.EnderecoEntrega;
import br.com.inovou.store.domain.pedido.ItemPedido;

import java.math.BigDecimal;
import java.util.List;

public record ResumoCheckout(List<ItemPedido> itens, Frete frete, EnderecoEntrega endereco) {

    public ResumoCheckout {
        itens = List.copyOf(itens);
    }

    public BigDecimal subtotal() {
        return itens.stream().map(ItemPedido::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal total() {
        return subtotal().add(frete.valor());
    }

    public BigDecimal descontoPix() {
        return DescontoPix.sobre(subtotal());
    }

    public BigDecimal totalNoPix() {
        return total().subtract(descontoPix());
    }
}
