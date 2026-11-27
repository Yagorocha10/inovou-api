package br.com.inovou.store.adapter.in.web.admin;

import br.com.inovou.store.adapter.in.web.pedido.EnderecoEntregaResponse;
import br.com.inovou.store.adapter.in.web.pedido.ItemPedidoResponse;
import br.com.inovou.store.adapter.in.web.pedido.PedidoResponse;
import br.com.inovou.store.application.consulta.PedidoComCliente;
import br.com.inovou.store.domain.pedido.Pedido;
import br.com.inovou.store.domain.pedido.StatusPedido;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PedidoAdminResponse(Long id, String codigo, StatusPedido status, LocalDateTime criadoEm,
                                  List<ItemPedidoResponse> itens, BigDecimal subtotal, BigDecimal frete,
                                  BigDecimal total, EnderecoEntregaResponse endereco,
                                  PedidoResponse.PagamentoResponse pagamento, boolean podeCancelar,
                                  ClienteResponse cliente, List<StatusPedido> proximosStatus) {

    static PedidoAdminResponse de(PedidoComCliente pedidoComCliente) {
        Pedido pedido = pedidoComCliente.pedido();
        PedidoResponse base = PedidoResponse.de(pedido);
        return new PedidoAdminResponse(base.id(), base.codigo(), base.status(), base.criadoEm(), base.itens(),
                base.subtotal(), base.frete(), base.total(), base.endereco(), base.pagamento(),
                base.podeCancelar(), ClienteResponse.de(pedidoComCliente.cliente()),
                pedido.proximosStatus().stream().sorted().toList());
    }
}
