package br.com.inovou.store.application.service;

import br.com.inovou.store.application.port.out.CarrinhoRepositoryPort;
import br.com.inovou.store.application.port.out.ProdutoRepositoryPort;
import br.com.inovou.store.domain.carrinho.Carrinho;
import br.com.inovou.store.domain.exception.RecursoNaoEncontradoException;
import br.com.inovou.store.domain.produto.Categoria;
import br.com.inovou.store.domain.produto.Produto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CarrinhoServiceTest {

    @Mock
    private CarrinhoRepositoryPort carrinhoRepository;

    @Mock
    private ProdutoRepositoryPort produtoRepository;

    private CarrinhoService service;

    private final Produto controle = new Produto(10L, "Controle PS4", null, null, new BigDecimal("129.90"), 5,
            true, new Categoria(3L, "Games e controles", null), List.of());

    @BeforeEach
    void setUp() {
        service = new CarrinhoService(carrinhoRepository, produtoRepository);
    }

    @Test
    void deveDevolverCarrinhoVazioSemGravarQuandoOUsuarioAindaNaoTemCarrinho() {
        when(carrinhoRepository.buscarDoUsuario(1L)).thenReturn(Optional.empty());

        Carrinho carrinho = service.buscar(1L);

        assertThat(carrinho.estaVazio()).isTrue();
        assertThat(carrinho.getUsuarioId()).isEqualTo(1L);
        verify(carrinhoRepository, never()).salvar(any());
    }

    @Test
    void deveAdicionarProdutoESalvar() {
        when(produtoRepository.buscarPorId(10L)).thenReturn(Optional.of(controle));
        when(carrinhoRepository.buscarDoUsuario(1L)).thenReturn(Optional.empty());
        when(carrinhoRepository.salvar(any(Carrinho.class))).thenAnswer(chamada -> chamada.getArgument(0));

        Carrinho carrinho = service.adicionar(1L, 10L, 2);

        assertThat(carrinho.quantidadeTotal()).isEqualTo(2);
        assertThat(carrinho.subtotal()).isEqualByComparingTo("259.80");
    }

    @Test
    void deveResponderNaoEncontradoParaProdutoInexistente() {
        when(produtoRepository.buscarPorId(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.adicionar(1L, 99L, 1)).isInstanceOf(RecursoNaoEncontradoException.class);
        verify(carrinhoRepository, never()).salvar(any());
    }
}
