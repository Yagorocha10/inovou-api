package br.com.inovou.store.adapter.in.web.pedido;

import br.com.inovou.store.domain.pedido.ItemPedido;

import java.math.BigDecimal;

public record ItemPedidoResponse(Long produtoId, String nome, String slug, String imagem,
                                 BigDecimal precoUnitario, int quantidade, BigDecimal subtotal) {

    static ItemPedidoResponse de(ItemPedido item) {
        return new ItemPedidoResponse(item.produtoId(), item.nome(), item.slug(), item.imagem(),
                item.precoUnitario(), item.quantidade(), item.subtotal());
    }
}
