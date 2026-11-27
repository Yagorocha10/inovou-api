package br.com.inovou.store.application.port.in;

import br.com.inovou.store.application.consulta.FiltroPedidosAdmin;
import br.com.inovou.store.application.consulta.Pagina;
import br.com.inovou.store.application.consulta.Paginacao;
import br.com.inovou.store.application.consulta.PedidoComCliente;
import br.com.inovou.store.domain.pedido.StatusPedido;

public interface GerenciarPedidosUseCase {

    Pagina<PedidoComCliente> listar(FiltroPedidosAdmin filtro, Paginacao paginacao);

    PedidoComCliente buscar(Long pedidoId);

    PedidoComCliente alterarStatus(Long pedidoId, StatusPedido novoStatus);
}
