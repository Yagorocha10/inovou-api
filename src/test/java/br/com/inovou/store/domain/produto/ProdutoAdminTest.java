package br.com.inovou.store.domain.produto;

import br.com.inovou.store.domain.exception.DomainException;
import br.com.inovou.store.domain.exception.RecursoNaoEncontradoException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProdutoAdminTest {

    private final Categoria eletronicos = new Categoria(4L, "Eletrônicos", null);
    private final Categoria games = new Categoria(3L, "Games e controles", null);

    private Produto comFotos(long... ids) {
        List<ImagemProduto> imagens = new ArrayList<>();
        for (int i = 0; i < ids.length; i++) {
            imagens.add(new ImagemProduto(ids[i], "/uploads/produtos/1/" + ids[i] + ".webp", i));
        }
        return new Produto(1L, "Fone Bluetooth", "fone-bluetooth", "", new BigDecimal("59.90"), 3, true,
                eletronicos, imagens);
    }

    @Test
    void atualizarTrocaTodosOsDadosDeUmaVez() {
        Produto produto = comFotos();

        produto.atualizar("Fone Bluetooth Pro", "Com cancelamento de ruído", new BigDecimal("89.90"), 7, games, false);

        assertThat(produto.getNome()).isEqualTo("Fone Bluetooth Pro");
        assertThat(produto.getDescricao()).isEqualTo("Com cancelamento de ruído");
        assertThat(produto.getPreco()).isEqualByComparingTo("89.90");
        assertThat(produto.getEstoque()).isEqualTo(7);
        assertThat(produto.getCategoria()).isEqualTo(games);
        assertThat(produto.isAtivo()).isFalse();
        assertThat(produto.getSlug()).isEqualTo("fone-bluetooth");
    }

    @Test
    void atualizarComUmCampoInvalidoNaoMudaNada() {
        Produto produto = comFotos();

        assertThatThrownBy(() -> produto.atualizar("Outro nome", "", new BigDecimal("10.00"), -1, games, true))
                .isInstanceOf(DomainException.class);

        assertThat(produto.getNome()).isEqualTo("Fone Bluetooth");
        assertThat(produto.getPreco()).isEqualByComparingTo("59.90");
    }

    @Test
    void recusaPrecoComMaisDeDuasCasasEDescricaoLongaDemais() {
        assertThatThrownBy(() -> Produto.novo("Cabo", null, new BigDecimal("59.999"), 1, eletronicos))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("2 casas");
        assertThatThrownBy(() -> Produto.novo("Cabo", "x".repeat(2001), new BigDecimal("10"), 1, eletronicos))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("2000");
        assertThat(Produto.novo("Cabo", "x".repeat(2000), new BigDecimal("10.500"), 1, eletronicos).getPreco())
                .isEqualByComparingTo("10.50");
    }

    @Test
    void definirEstoqueSubstituiOValor() {
        Produto produto = comFotos();

        produto.definirEstoque(0);

        assertThat(produto.getEstoque()).isZero();
        assertThat(produto.disponivelParaVenda()).isFalse();
        assertThatThrownBy(() -> produto.definirEstoque(-2)).isInstanceOf(DomainException.class);
    }

    @Test
    void aceitaAteOitoFotos() {
        Produto produto = comFotos(1, 2, 3, 4, 5, 6, 7);

        produto.adicionarImagem("/uploads/produtos/1/oitava.webp");

        assertThat(produto.getImagens()).hasSize(8);
        assertThatThrownBy(() -> produto.adicionarImagem("/uploads/produtos/1/nona.webp"))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("8 fotos");
    }

    @Test
    void removerFotoRenumeraAOrdemEPromoveAProximaAPrincipal() {
        Produto produto = comFotos(10, 20, 30);

        ImagemProduto removida = produto.removerImagem(10L);

        assertThat(removida.url()).endsWith("10.webp");
        assertThat(produto.getImagens()).extracting(ImagemProduto::id).containsExactly(20L, 30L);
        assertThat(produto.getImagens()).extracting(ImagemProduto::ordem).containsExactly(0, 1);
        assertThat(produto.imagemPrincipal()).get().extracting(ImagemProduto::id).isEqualTo(20L);
        assertThatThrownBy(() -> produto.removerImagem(99L)).isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    void reordenarUsaAPosicaoDaListaComoOrdem() {
        Produto produto = comFotos(10, 20, 30);

        produto.reordenarImagens(List.of(30L, 10L, 20L));

        assertThat(produto.getImagens()).extracting(ImagemProduto::id).containsExactly(30L, 10L, 20L);
        assertThat(produto.getImagens()).extracting(ImagemProduto::ordem).containsExactly(0, 1, 2);
    }

    @Test
    void reordenarExigeTodasAsFotosSemRepetir() {
        Produto produto = comFotos(10, 20, 30);

        assertThatThrownBy(() -> produto.reordenarImagens(List.of(30L, 10L))).isInstanceOf(DomainException.class);
        assertThatThrownBy(() -> produto.reordenarImagens(List.of(30L, 30L, 10L))).isInstanceOf(DomainException.class);
        assertThatThrownBy(() -> produto.reordenarImagens(List.of(30L, 10L, 99L))).isInstanceOf(DomainException.class);
        assertThat(produto.getImagens()).extracting(ImagemProduto::id).containsExactly(10L, 20L, 30L);
    }
}
