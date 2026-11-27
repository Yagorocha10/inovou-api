package br.com.inovou.store.application.consulta;

import br.com.inovou.store.domain.pedido.Pedido;
import br.com.inovou.store.domain.usuario.Usuario;

public record PedidoComCliente(Pedido pedido, Usuario cliente) {
}
