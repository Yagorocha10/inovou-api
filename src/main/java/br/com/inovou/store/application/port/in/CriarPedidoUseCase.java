package br.com.inovou.store.application.port.in;

import br.com.inovou.store.domain.pedido.FormaPagamento;
import br.com.inovou.store.domain.pedido.Pedido;

public interface CriarPedidoUseCase {

    Pedido criar(Long usuarioId, Long enderecoId, FormaPagamento formaPagamento);
}
