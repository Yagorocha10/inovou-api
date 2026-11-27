package br.com.inovou.store.application.service;

import br.com.inovou.store.application.consulta.FiltroPedidosAdmin;
import br.com.inovou.store.application.consulta.Pagina;
import br.com.inovou.store.application.consulta.Paginacao;
import br.com.inovou.store.application.consulta.PedidoComCliente;
import br.com.inovou.store.application.port.in.GerenciarPedidosUseCase;
import br.com.inovou.store.application.port.out.PedidoRepositoryPort;
import br.com.inovou.store.application.port.out.ProdutoRepositoryPort;
import br.com.inovou.store.application.port.out.TransacaoPort;
import br.com.inovou.store.application.port.out.UsuarioRepositoryPort;
import br.com.inovou.store.domain.exception.DomainException;
import br.com.inovou.store.domain.exception.RecursoNaoEncontradoException;
import br.com.inovou.store.domain.pedido.Pedido;
import br.com.inovou.store.domain.pedido.StatusPedido;
import br.com.inovou.store.domain.usuario.Usuario;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PedidoAdminService implements GerenciarPedidosUseCase {

    private final PedidoRepositoryPort pedidoRepository;
    private final UsuarioRepositoryPort usuarioRepository;
    private final ProdutoRepositoryPort produtoRepository;
    private final TransacaoPort transacao;

    public PedidoAdminService(PedidoRepositoryPort pedidoRepository, UsuarioRepositoryPort usuarioRepository,
                              ProdutoRepositoryPort produtoRepository, TransacaoPort transacao) {
        this.pedidoRepository = pedidoRepository;
        this.usuarioRepository = usuarioRepository;
        this.produtoRepository = produtoRepository;
        this.transacao = transacao;
    }

    @Override
    public Pagina<PedidoComCliente> listar(FiltroPedidosAdmin filtro, Paginacao paginacao) {
        Pagina<Pedido> pagina = pedidoRepository.listarParaAdmin(filtro, paginacao);
        Set<Long> idsClientes = pagina.itens().stream().map(Pedido::getUsuarioId).collect(Collectors.toSet());
        Map<Long, Usuario> clientes = idsClientes.isEmpty()
                ? Map.of()
                : usuarioRepository.buscarPorIds(idsClientes).stream()
                        .collect(Collectors.toMap(Usuario::getId, Function.identity()));
        List<PedidoComCliente> itens = pagina.itens().stream()
                .map(pedido -> new PedidoComCliente(pedido, clientes.get(pedido.getUsuarioId())))
                .toList();
        return new Pagina<>(itens, pagina.pagina(), pagina.tamanho(), pagina.totalItens());
    }

    @Override
    public PedidoComCliente buscar(Long pedidoId) {
        return comCliente(pedido(pedidoId));
    }

    @Override
    public PedidoComCliente alterarStatus(Long pedidoId, StatusPedido novoStatus) {
        if (novoStatus == null) {
            throw new DomainException("Informe o novo status do pedido");
        }
        Pedido atualizado = transacao.executar(() -> {
            Pedido pedido = pedido(pedidoId);
            pedido.avancarPara(novoStatus);
            if (novoStatus == StatusPedido.CANCELADO) {
                pedido.getItens().forEach(item -> produtoRepository.reporEstoque(item.produtoId(), item.quantidade()));
            }
            return pedidoRepository.salvar(pedido);
        });
        return comCliente(atualizado);
    }

    private Pedido pedido(Long pedidoId) {
        return pedidoRepository.buscarPorId(pedidoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pedido não encontrado"));
    }

    private PedidoComCliente comCliente(Pedido pedido) {
        return new PedidoComCliente(pedido, usuarioRepository.buscarPorId(pedido.getUsuarioId()).orElse(null));
    }
}
