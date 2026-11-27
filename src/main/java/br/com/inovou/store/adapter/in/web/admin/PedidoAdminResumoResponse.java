package br.com.inovou.store.adapter.in.web.admin;

import br.com.inovou.store.application.consulta.PedidoComCliente;
import br.com.inovou.store.domain.pedido.FormaPagamento;
import br.com.inovou.store.domain.pedido.Pedido;
import br.com.inovou.store.domain.pedido.StatusPedido;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PedidoAdminResumoResponse(Long id, String codigo, StatusPedido status, LocalDateTime criadoEm,
                                        ClienteResponse cliente, FormaPagamento formaPagamento,
                                        int quantidadeItens, BigDecimal total) {

    static PedidoAdminResumoResponse de(PedidoComCliente pedidoComCliente) {
        Pedido pedido = pedidoComCliente.pedido();
        return new PedidoAdminResumoResponse(pedido.getId(), pedido.getCodigo(), pedido.getStatus(),
                pedido.getCriadoEm(), ClienteResponse.de(pedidoComCliente.cliente()),
                pedido.getPagamento().forma(), pedido.quantidadeItens(), pedido.total());
    }
}
