package br.com.inovou.store.application.service;

import br.com.inovou.store.application.consulta.FiltroPedidosAdmin;
import br.com.inovou.store.application.consulta.Pagina;
import br.com.inovou.store.application.consulta.Paginacao;
import br.com.inovou.store.application.consulta.PedidoComCliente;
import br.com.inovou.store.application.port.out.PedidoRepositoryPort;
import br.com.inovou.store.application.port.out.ProdutoRepositoryPort;
import br.com.inovou.store.application.port.out.TransacaoPort;
import br.com.inovou.store.application.port.out.UsuarioRepositoryPort;
import br.com.inovou.store.domain.exception.RecursoNaoEncontradoException;
import br.com.inovou.store.domain.exception.TransicaoInvalidaException;
import br.com.inovou.store.domain.pedido.EnderecoEntrega;
import br.com.inovou.store.domain.pedido.FormaPagamento;
import br.com.inovou.store.domain.pedido.ItemPedido;
import br.com.inovou.store.domain.pedido.Pagamento;
import br.com.inovou.store.domain.pedido.Pedido;
import br.com.inovou.store.domain.pedido.StatusPedido;
import br.com.inovou.store.domain.usuario.Role;
import br.com.inovou.store.domain.usuario.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PedidoAdminServiceTest {

    @Mock
    private PedidoRepositoryPort pedidoRepository;
    @Mock
    private UsuarioRepositoryPort usuarioRepository;
    @Mock
    private ProdutoRepositoryPort produtoRepository;

    private final TransacaoPort transacaoDireta = new TransacaoPort() {
        @Override
        public <T> T executar(Supplier<T> operacao) {
            return operacao.get();
        }
    };

    private PedidoAdminService service;

    private final Usuario maria = new Usuario(3L, "Maria", "maria@teste.com", "hash", Role.CLIENTE);

    @BeforeEach
    void setUp() {
        service = new PedidoAdminService(pedidoRepository, usuarioRepository, produtoRepository, transacaoDireta);
    }

    private Pedido pedido(long id, StatusPedido status) {
        return new Pedido(id, "INV-%06d".formatted(id), 3L, status, LocalDateTime.of(2026, 11, 27, 9, 0),
                List.of(new ItemPedido(10L, "Controle", "controle", null, new BigDecimal("129.90"), 2)),
                new BigDecimal("10.00"),
                new EnderecoEntrega("66000-000", "Rua A", "10", null, "Nazaré", "Belém", "PA"),
                Pagamento.pendente(FormaPagamento.NA_ENTREGA));
    }

    @Test
    void listaJuntandoOsClientesNumaConsultaSo() {
        when(pedidoRepository.listarParaAdmin(any(), any()))
                .thenReturn(new Pagina<>(List.of(pedido(1, StatusPedido.PAGO), pedido(2, StatusPedido.ENTREGUE)), 0, 20, 2));
        when(usuarioRepository.buscarPorIds(Set.of(3L))).thenReturn(List.of(maria));

        Pagina<PedidoComCliente> pagina = service.listar(new FiltroPedidosAdmin(null, "maria"), Paginacao.de(0, 20));

        assertThat(pagina.itens()).hasSize(2);
        assertThat(pagina.itens()).allSatisfy(item -> assertThat(item.cliente().getEmail()).isEqualTo("maria@teste.com"));
    }

    @Test
    void avancaParaOProximoStatus() {
        Pedido pedido = pedido(1, StatusPedido.AGUARDANDO_PAGAMENTO);
        when(pedidoRepository.buscarPorId(1L)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.salvar(pedido)).thenReturn(pedido);
        when(usuarioRepository.buscarPorId(3L)).thenReturn(Optional.of(maria));

        PedidoComCliente resultado = service.alterarStatus(1L, StatusPedido.PAGO);

        assertThat(resultado.pedido().getStatus()).isEqualTo(StatusPedido.PAGO);
        assertThat(resultado.pedido().proximosStatus()).containsExactlyInAnyOrder(StatusPedido.EM_SEPARACAO,
                StatusPedido.CANCELADO);
        verify(produtoRepository, never()).reporEstoque(anyLong(), anyInt());
    }

    @Test
    void naoDeixaPularEtapas() {
        when(pedidoRepository.buscarPorId(1L)).thenReturn(Optional.of(pedido(1, StatusPedido.PAGO)));

        assertThatThrownBy(() -> service.alterarStatus(1L, StatusPedido.ENTREGUE))
                .isInstanceOf(TransicaoInvalidaException.class);
        verify(pedidoRepository, never()).salvar(any());
    }

    @Test
    void cancelarPeloAdminDevolveOEstoque() {
        Pedido pedido = pedido(1, StatusPedido.EM_SEPARACAO);
        when(pedidoRepository.buscarPorId(1L)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.salvar(pedido)).thenReturn(pedido);
        when(usuarioRepository.buscarPorId(3L)).thenReturn(Optional.of(maria));

        service.alterarStatus(1L, StatusPedido.CANCELADO);

        verify(produtoRepository).reporEstoque(10L, 2);
    }

    @Test
    void pedidoInexistenteResponde404() {
        when(pedidoRepository.buscarPorId(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscar(99L)).isInstanceOf(RecursoNaoEncontradoException.class);
    }
}
