package br.com.inovou.store.application.service;

import br.com.inovou.store.application.consulta.ResumoCheckout;
import br.com.inovou.store.application.port.out.CarrinhoRepositoryPort;
import br.com.inovou.store.application.port.out.EnderecoRepositoryPort;
import br.com.inovou.store.application.port.out.PagamentoPort;
import br.com.inovou.store.application.port.out.PedidoRepositoryPort;
import br.com.inovou.store.application.port.out.ProdutoRepositoryPort;
import br.com.inovou.store.application.port.out.TransacaoPort;
import br.com.inovou.store.domain.carrinho.Carrinho;
import br.com.inovou.store.domain.exception.DomainException;
import br.com.inovou.store.domain.exception.EntregaIndisponivelException;
import br.com.inovou.store.domain.exception.EstoqueInsuficienteException;
import br.com.inovou.store.domain.exception.RecursoNaoEncontradoException;
import br.com.inovou.store.domain.pedido.EnderecoEntrega;
import br.com.inovou.store.domain.pedido.FormaPagamento;
import br.com.inovou.store.domain.pedido.ItemPedido;
import br.com.inovou.store.domain.pedido.Pagamento;
import br.com.inovou.store.domain.pedido.Pedido;
import br.com.inovou.store.domain.pedido.StatusPedido;
import br.com.inovou.store.domain.produto.Categoria;
import br.com.inovou.store.domain.produto.Produto;
import br.com.inovou.store.domain.usuario.Endereco;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
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
class PedidoServiceTest {

    private static final long USUARIO = 1L;
    private static final long ENDERECO = 5L;

    @Mock
    private PedidoRepositoryPort pedidoRepository;
    @Mock
    private CarrinhoRepositoryPort carrinhoRepository;
    @Mock
    private EnderecoRepositoryPort enderecoRepository;
    @Mock
    private ProdutoRepositoryPort produtoRepository;
    @Mock
    private PagamentoPort pagamento;

    private final TransacaoPort transacaoDireta = new TransacaoPort() {
        @Override
        public <T> T executar(Supplier<T> operacao) {
            return operacao.get();
        }
    };

    private PedidoService service;

    private final Produto controle = new Produto(10L, "Controle PS4", null, null, new BigDecimal("129.90"), 5,
            true, new Categoria(3L, "Games e controles", null), List.of());

    @BeforeEach
    void setUp() {
        Clock relogio = Clock.fixed(Instant.parse("2026-11-25T13:00:00Z"), ZoneId.of("America/Belem"));
        service = new PedidoService(pedidoRepository, carrinhoRepository, enderecoRepository, produtoRepository,
                pagamento, transacaoDireta, relogio);
    }

    private Endereco endereco(String cep) {
        return new Endereco(ENDERECO, USUARIO, cep, "Rua A", "10", null, "Nazaré", "Belém", "PA");
    }

    private Carrinho carrinhoCom(int quantidade) {
        Carrinho carrinho = Carrinho.vazio(USUARIO);
        carrinho.adicionar(controle, quantidade);
        return carrinho;
    }

    @Test
    void resumoSomaOFreteDoCep() {
        when(enderecoRepository.buscarDoUsuario(ENDERECO, USUARIO)).thenReturn(Optional.of(endereco("66010-000")));
        when(carrinhoRepository.buscarDoUsuario(USUARIO)).thenReturn(Optional.of(carrinhoCom(2)));

        ResumoCheckout resumo = service.resumir(USUARIO, ENDERECO);

        assertThat(resumo.subtotal()).isEqualByComparingTo("259.80");
        assertThat(resumo.frete().valor()).isEqualByComparingTo("10.00");
        assertThat(resumo.total()).isEqualByComparingTo("269.80");
    }

    @Test
    void criarBaixaEstoqueRegistraPagamentoEEsvaziaOCarrinho() {
        Carrinho carrinho = carrinhoCom(2);
        when(enderecoRepository.buscarDoUsuario(ENDERECO, USUARIO)).thenReturn(Optional.of(endereco("66010-000")));
        when(carrinhoRepository.buscarDoUsuario(USUARIO)).thenReturn(Optional.of(carrinho));
        when(produtoRepository.baixarEstoque(10L, 2)).thenReturn(true);
        when(pedidoRepository.salvar(any(Pedido.class))).thenAnswer(chamada -> chamada.getArgument(0));
        when(pagamento.iniciar(any(Pedido.class)))
                .thenReturn(new Pagamento(FormaPagamento.CARTAO, true, "Aprovado", null));

        Pedido pedido = service.criar(USUARIO, ENDERECO, FormaPagamento.CARTAO);

        assertThat(pedido.getStatus()).isEqualTo(StatusPedido.PAGO);
        assertThat(pedido.total()).isEqualByComparingTo("269.80");
        assertThat(pedido.getItens().getFirst().precoUnitario()).isEqualByComparingTo("129.90");
        assertThat(pedido.getCriadoEm()).isEqualTo("2026-11-25T10:00:00");
        assertThat(carrinho.estaVazio()).isTrue();
        verify(carrinhoRepository).salvar(carrinho);
    }

    @Test
    void estoqueQueAcabouNoMeioDoCaminhoImpedeOPedido() {
        when(enderecoRepository.buscarDoUsuario(ENDERECO, USUARIO)).thenReturn(Optional.of(endereco("66010-000")));
        when(carrinhoRepository.buscarDoUsuario(USUARIO)).thenReturn(Optional.of(carrinhoCom(2)));
        when(produtoRepository.baixarEstoque(10L, 2)).thenReturn(false);
        when(produtoRepository.buscarPorId(10L)).thenReturn(Optional.of(controle));

        assertThatThrownBy(() -> service.criar(USUARIO, ENDERECO, FormaPagamento.PIX))
                .isInstanceOf(EstoqueInsuficienteException.class);
        verify(pedidoRepository, never()).salvar(any());
        verify(carrinhoRepository, never()).salvar(any());
    }

    @Test
    void cepForaDaAreaNaoGeraPedido() {
        when(enderecoRepository.buscarDoUsuario(ENDERECO, USUARIO)).thenReturn(Optional.of(endereco("01310-100")));

        assertThatThrownBy(() -> service.criar(USUARIO, ENDERECO, FormaPagamento.PIX))
                .isInstanceOf(EntregaIndisponivelException.class);
        verify(produtoRepository, never()).baixarEstoque(anyLong(), anyInt());
    }

    @Test
    void carrinhoVazioNaoGeraPedido() {
        when(enderecoRepository.buscarDoUsuario(ENDERECO, USUARIO)).thenReturn(Optional.of(endereco("66010-000")));
        when(carrinhoRepository.buscarDoUsuario(USUARIO)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.criar(USUARIO, ENDERECO, FormaPagamento.PIX))
                .isInstanceOf(DomainException.class)
                .hasMessage("Seu carrinho está vazio");
    }

    @Test
    void enderecoDeOutroClienteNaoEhEncontrado() {
        when(enderecoRepository.buscarDoUsuario(ENDERECO, USUARIO)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.resumir(USUARIO, ENDERECO))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    void pedidoDeOutroClienteNaoEhEncontrado() {
        Pedido deOutro = new Pedido(9L, "INV-000009", 99L, StatusPedido.PAGO, null,
                List.of(new ItemPedido(10L, "Controle", "controle", null,
                        new BigDecimal("10.00"), 1)),
                BigDecimal.TEN, new EnderecoEntrega("66000-000", "Rua", "1",
                null, "Centro", "Belém", "PA"), Pagamento.pendente(FormaPagamento.PIX));
        when(pedidoRepository.buscarPorId(9L)).thenReturn(Optional.of(deOutro));

        assertThatThrownBy(() -> service.buscar(USUARIO, 9L)).isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    void cancelarDevolveOEstoque() {
        Pedido meu = new Pedido(9L, "INV-000009", USUARIO, StatusPedido.AGUARDANDO_PAGAMENTO, null,
                List.of(new ItemPedido(10L, "Controle", "controle", null,
                        new BigDecimal("10.00"), 3)),
                BigDecimal.TEN, new EnderecoEntrega("66000-000", "Rua", "1",
                null, "Centro", "Belém", "PA"), Pagamento.pendente(FormaPagamento.PIX));
        when(pedidoRepository.buscarPorId(9L)).thenReturn(Optional.of(meu));
        when(pedidoRepository.salvar(meu)).thenReturn(meu);

        Pedido cancelado = service.cancelar(USUARIO, 9L);

        assertThat(cancelado.getStatus()).isEqualTo(StatusPedido.CANCELADO);
        verify(produtoRepository).reporEstoque(10L, 3);
    }
}
