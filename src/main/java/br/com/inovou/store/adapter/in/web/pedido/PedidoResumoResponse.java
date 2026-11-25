package br.com.inovou.store.adapter.in.web.pedido;

import br.com.inovou.store.domain.pedido.Pedido;
import br.com.inovou.store.domain.pedido.StatusPedido;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PedidoResumoResponse(Long id, String codigo, StatusPedido status, LocalDateTime criadoEm,
                                   int quantidadeItens, BigDecimal total) {

    static PedidoResumoResponse de(Pedido pedido) {
        return new PedidoResumoResponse(pedido.getId(), pedido.getCodigo(), pedido.getStatus(),
                pedido.getCriadoEm(), pedido.quantidadeItens(), pedido.total());
    }
}
