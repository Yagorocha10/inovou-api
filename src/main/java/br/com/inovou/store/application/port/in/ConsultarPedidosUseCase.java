package br.com.inovou.store.application.port.in;

import br.com.inovou.store.application.consulta.Pagina;
import br.com.inovou.store.application.consulta.Paginacao;
import br.com.inovou.store.domain.pedido.Pedido;

public interface ConsultarPedidosUseCase {

    Pagina<Pedido> listar(Long usuarioId, Paginacao paginacao);

    Pedido buscar(Long usuarioId, Long pedidoId);
}
