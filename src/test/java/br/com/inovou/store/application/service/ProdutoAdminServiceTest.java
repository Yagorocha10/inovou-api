package br.com.inovou.store.application.service;

import br.com.inovou.store.application.consulta.ArquivoImagem;
import br.com.inovou.store.application.consulta.DadosProduto;
import br.com.inovou.store.application.port.out.ArmazenamentoImagemPort;
import br.com.inovou.store.application.port.out.CategoriaRepositoryPort;
import br.com.inovou.store.application.port.out.ProdutoRepositoryPort;
import br.com.inovou.store.domain.exception.DomainException;
import br.com.inovou.store.domain.exception.ProdutoDuplicadoException;
import br.com.inovou.store.domain.exception.RecursoNaoEncontradoException;
import br.com.inovou.store.domain.produto.Categoria;
import br.com.inovou.store.domain.produto.ImagemProduto;
import br.com.inovou.store.domain.produto.Produto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProdutoAdminServiceTest {

    @Mock
    private ProdutoRepositoryPort produtoRepository;

    @Mock
    private CategoriaRepositoryPort categoriaRepository;

    @Mock
    private ArmazenamentoImagemPort armazenamento;

    private ProdutoAdminService service;

    private final Categoria eletronicos = new Categoria(1L, "Eletrônicos", "eletronicos");

    private final DadosProduto fone = new DadosProduto("  Fone Bluetooth  ", "Sem fio", new BigDecimal("59.90"), 3,
            1L, true);

    @BeforeEach
    void setUp() {
        service = new ProdutoAdminService(produtoRepository, categoriaRepository, armazenamento);
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

    private Produto comFotos(int quantidade) {
        List<ImagemProduto> imagens = new ArrayList<>();
        for (int i = 0; i < quantidade; i++) {
            imagens.add(new ImagemProduto((long) i + 1, "/uploads/produtos/7/" + (i + 1) + ".png", i));
        }
        return new Produto(7L, "Fone", "fone", "", new BigDecimal("49.90"), 1, true, eletronicos, imagens);
    }

    private ArquivoImagem foto() {
        return new ArquivoImagem("foto.png", "image/png", 3, new ByteArrayInputStream(new byte[]{1, 2, 3}));
    }

    @Test
    void adicionaFotoGravadaNoDisco() {
        Produto produto = comFotos(1);
        when(produtoRepository.buscarPorId(7L)).thenReturn(Optional.of(produto));
        when(armazenamento.salvar(eq(7L), any())).thenReturn("/uploads/produtos/7/nova.png");
        when(produtoRepository.salvar(produto)).thenReturn(produto);

        Produto atualizado = service.adicionar(7L, foto());

        assertThat(atualizado.getImagens()).extracting(ImagemProduto::url).last()
                .isEqualTo("/uploads/produtos/7/nova.png");
    }

    @Test
    void apagaDoDiscoAFotoQueNaoPodeSerAdicionada() {
        when(produtoRepository.buscarPorId(7L)).thenReturn(Optional.of(comFotos(8)));
        when(armazenamento.salvar(eq(7L), any())).thenReturn("/uploads/produtos/7/nona.png");

        assertThatThrownBy(() -> service.adicionar(7L, foto())).isInstanceOf(DomainException.class);
        verify(armazenamento).remover("/uploads/produtos/7/nona.png");
        verify(produtoRepository, never()).salvar(any());
    }

    @Test
    void removerApagaOArquivoDepoisDeSalvar() {
        Produto produto = comFotos(2);
        when(produtoRepository.buscarPorId(7L)).thenReturn(Optional.of(produto));
        when(produtoRepository.salvar(produto)).thenReturn(produto);

        Produto atualizado = service.remover(7L, 1L);

        assertThat(atualizado.getImagens()).extracting(ImagemProduto::id).containsExactly(2L);
        InOrder ordem = inOrder(produtoRepository, armazenamento);
        ordem.verify(produtoRepository).salvar(produto);
        ordem.verify(armazenamento).remover("/uploads/produtos/7/1.png");
    }
}
