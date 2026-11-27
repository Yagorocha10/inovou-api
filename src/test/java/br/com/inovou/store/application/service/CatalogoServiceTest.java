package br.com.inovou.store.application.service;

import br.com.inovou.store.application.consulta.FiltroProdutos;
import br.com.inovou.store.application.consulta.Pagina;
import br.com.inovou.store.application.consulta.Paginacao;
import br.com.inovou.store.application.port.out.ProdutoRepositoryPort;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CatalogoServiceTest {

    @Mock
    private ProdutoRepositoryPort produtoRepository;

    private CatalogoService catalogoService;

    private final Produto controle = new Produto(1L, "Controle PS4 sem fio Bluetooth", null, null,
            new BigDecimal("129.90"), 3, true, new Categoria(1L, "Games e controles", null), List.of());

    @BeforeEach
    void setUp() {
        catalogoService = new CatalogoService(produtoRepository);
    }

    @Test
    void deveBuscarProdutosNoRepositorio() {
        FiltroProdutos filtro = new FiltroProdutos("controle", null, null);
        Paginacao paginacao = new Paginacao(0, 12);
        Pagina<Produto> esperada = new Pagina<>(List.of(controle), 0, 12, 1);
        when(produtoRepository.buscarAtivos(filtro, paginacao)).thenReturn(esperada);

        Pagina<Produto> resultado = catalogoService.buscar(filtro, paginacao);

        assertThat(resultado).isEqualTo(esperada);
    }

    @Test
    void deveDetalharProdutoExistente() {
        when(produtoRepository.buscarAtivoPorSlug("controle-ps4-sem-fio-bluetooth")).thenReturn(Optional.of(controle));

        Produto resultado = catalogoService.detalhar("controle-ps4-sem-fio-bluetooth");

        assertThat(resultado).isSameAs(controle);
    }

    @Test
    void deveLancarNaoEncontradoQuandoSlugNaoExiste() {
        when(produtoRepository.buscarAtivoPorSlug("nao-existe")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> catalogoService.detalhar("nao-existe"))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessageContaining("nao-existe");
    }

    @Test
    void deveLimitarQuantidadeDeDestaques() {
        when(produtoRepository.buscarDisponiveisMaisRecentes(CatalogoService.LIMITE_MAXIMO_DESTAQUES))
                .thenReturn(List.of(controle));

        List<Produto> resultado = catalogoService.listarDestaques(500);

        assertThat(resultado).containsExactly(controle);
        verify(produtoRepository).buscarDisponiveisMaisRecentes(CatalogoService.LIMITE_MAXIMO_DESTAQUES);
    }

    @Test
    void deveUsarPeloMenosUmDestaque() {
        when(produtoRepository.buscarDisponiveisMaisRecentes(1)).thenReturn(List.of());

        catalogoService.listarDestaques(0);

        verify(produtoRepository).buscarDisponiveisMaisRecentes(1);
    }

    @Test
    void deveLimitarQuantidadeDeMaisVendidos() {
        catalogoService.listarMaisVendidos(500);

        verify(produtoRepository).buscarMaisVendidos(CatalogoService.LIMITE_MAXIMO_DESTAQUES);
    }
}
