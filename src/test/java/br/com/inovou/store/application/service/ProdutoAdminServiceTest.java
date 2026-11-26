package br.com.inovou.store.application.service;

import br.com.inovou.store.application.consulta.DadosProduto;
import br.com.inovou.store.application.port.out.CategoriaRepositoryPort;
import br.com.inovou.store.application.port.out.ProdutoRepositoryPort;
import br.com.inovou.store.domain.exception.DomainException;
import br.com.inovou.store.domain.exception.ProdutoDuplicadoException;
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
class ProdutoAdminServiceTest {

    @Mock
    private ProdutoRepositoryPort produtoRepository;

    @Mock
    private CategoriaRepositoryPort categoriaRepository;

    private ProdutoAdminService service;

    private final Categoria eletronicos = new Categoria(1L, "Eletrônicos", "eletronicos");

    private final DadosProduto fone = new DadosProduto("  Fone Bluetooth  ", "Sem fio", new BigDecimal("59.90"), 3,
            1L, true);

    @BeforeEach
    void setUp() {
        service = new ProdutoAdminService(produtoRepository, categoriaRepository);
    }

    @Test
    void criaProdutoComNomeLimpoESalva() {
        when(categoriaRepository.buscarPorId(1L)).thenReturn(Optional.of(eletronicos));
        when(produtoRepository.existeComNome("Fone Bluetooth", null)).thenReturn(false);
        when(produtoRepository.salvar(any(Produto.class))).thenAnswer(chamada -> chamada.getArgument(0));

        Produto criado = service.criar(fone);

        assertThat(criado.getNome()).isEqualTo("Fone Bluetooth");
        assertThat(criado.getSlug()).isEqualTo("fone-bluetooth");
        assertThat(criado.isAtivo()).isTrue();
    }

    @Test
    void naoCriaProdutoComNomeRepetido() {
        when(categoriaRepository.buscarPorId(1L)).thenReturn(Optional.of(eletronicos));
        when(produtoRepository.existeComNome("Fone Bluetooth", null)).thenReturn(true);

        assertThatThrownBy(() -> service.criar(fone)).isInstanceOf(ProdutoDuplicadoException.class);
        verify(produtoRepository, never()).salvar(any());
    }

    @Test
    void categoriaInexistenteResponde404() {
        when(categoriaRepository.buscarPorId(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.criar(fone)).isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    void atualizarIgnoraOProprioProdutoNaChecagemDeNome() {
        Produto existente = new Produto(7L, "Fone Bluetooth", "fone-bluetooth", "", new BigDecimal("49.90"), 1, true,
                eletronicos, List.of());
        when(produtoRepository.buscarPorId(7L)).thenReturn(Optional.of(existente));
        when(categoriaRepository.buscarPorId(1L)).thenReturn(Optional.of(eletronicos));
        when(produtoRepository.existeComNome("Fone Bluetooth", 7L)).thenReturn(false);
        when(produtoRepository.salvar(existente)).thenReturn(existente);

        Produto atualizado = service.atualizar(7L, fone);

        assertThat(atualizado.getPreco()).isEqualByComparingTo("59.90");
        assertThat(atualizado.getEstoque()).isEqualTo(3);
    }

    @Test
    void dadosInvalidosNaoChegamAoBanco() {
        when(categoriaRepository.buscarPorId(1L)).thenReturn(Optional.of(eletronicos));
        DadosProduto precoInvalido = new DadosProduto("Cabo", "", new BigDecimal("0"), 1, 1L, true);

        assertThatThrownBy(() -> service.criar(precoInvalido)).isInstanceOf(DomainException.class);
        verify(produtoRepository, never()).salvar(any());
    }

    @Test
    void desativarTiraDaVitrine() {
        Produto existente = new Produto(7L, "Fone", "fone", "", new BigDecimal("49.90"), 1, true, eletronicos,
                List.of());
        when(produtoRepository.buscarPorId(7L)).thenReturn(Optional.of(existente));
        when(produtoRepository.salvar(existente)).thenReturn(existente);

        assertThat(service.alterarAtivo(7L, false).disponivelParaVenda()).isFalse();
    }
}
