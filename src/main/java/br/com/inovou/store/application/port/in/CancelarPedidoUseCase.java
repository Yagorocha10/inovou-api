package br.com.inovou.store.application.port.in;

import br.com.inovou.store.domain.pedido.Pedido;

public interface CancelarPedidoUseCase {

    Pedido cancelar(Long usuarioId, Long pedidoId);
}
