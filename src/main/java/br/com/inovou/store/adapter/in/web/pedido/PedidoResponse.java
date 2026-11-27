package br.com.inovou.store.adapter.in.web.pedido;

import br.com.inovou.store.domain.pedido.FormaPagamento;
import br.com.inovou.store.domain.pedido.Pedido;
import br.com.inovou.store.domain.pedido.StatusPedido;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PedidoResponse(Long id, String codigo, StatusPedido status, LocalDateTime criadoEm,
                             List<ItemPedidoResponse> itens, BigDecimal subtotal, BigDecimal frete, BigDecimal total,
                             EnderecoEntregaResponse endereco, PagamentoResponse pagamento, boolean podeCancelar) {

    public record PagamentoResponse(FormaPagamento forma, String instrucoes, String codigoPix) {
    }

    public static PedidoResponse de(Pedido pedido) {
        return new PedidoResponse(
                pedido.getId(),
                pedido.getCodigo(),
                pedido.getStatus(),
                pedido.getCriadoEm(),
                pedido.getItens().stream().map(ItemPedidoResponse::de).toList(),
                pedido.subtotal(),
                pedido.getFrete(),
                pedido.total(),
                EnderecoEntregaResponse.de(pedido.getEndereco()),
                new PagamentoResponse(pedido.getPagamento().forma(), pedido.getPagamento().instrucoes(),
                        pedido.getPagamento().codigoPix()),
                pedido.podeSerCanceladoPeloCliente());
    }
}
