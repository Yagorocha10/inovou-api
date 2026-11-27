package br.com.inovou.store.application.consulta;

import br.com.inovou.store.domain.pedido.StatusPedido;

public record FiltroPedidosAdmin(StatusPedido status, String busca) {

    public FiltroPedidosAdmin {
        busca = busca == null ? "" : busca.strip();
    }
}
