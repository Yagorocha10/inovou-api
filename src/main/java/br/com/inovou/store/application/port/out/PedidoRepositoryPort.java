package br.com.inovou.store.application.port.out;

import br.com.inovou.store.application.consulta.FiltroPedidosAdmin;
import br.com.inovou.store.application.consulta.Pagina;
import br.com.inovou.store.application.consulta.Paginacao;
import br.com.inovou.store.domain.pedido.Pedido;

import java.util.Optional;

public interface PedidoRepositoryPort {

    Pedido salvar(Pedido pedido);

    Optional<Pedido> buscarPorId(Long id);

    Pagina<Pedido> listarDoUsuario(Long usuarioId, Paginacao paginacao);

    Pagina<Pedido> listarParaAdmin(FiltroPedidosAdmin filtro, Paginacao paginacao);
}
