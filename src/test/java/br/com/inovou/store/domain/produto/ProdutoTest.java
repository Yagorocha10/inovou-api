package br.com.inovou.store.domain.produto;

import br.com.inovou.store.domain.exception.DomainException;
import br.com.inovou.store.domain.exception.EstoqueInsuficienteException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProdutoTest {

    private final Categoria games = new Categoria(1L, "Games e controles", "games-e-controles");

    private Produto controleComEstoque(int estoque) {
        return Produto.novo("Controle PS4 sem fio", "Bluetooth", new BigDecimal("129.9"), estoque, games);
    }

    @Test
    void deveCriarProdutoAtivoComSlugEPrecoComDuasCasas() {
        Produto produto = controleComEstoque(10);

        assertThat(produto.getSlug()).isEqualTo("controle-ps4-sem-fio");
        assertThat(produto.getPreco()).isEqualByComparingTo("129.90");
        assertThat(produto.getPreco().scale()).isEqualTo(2);
        assertThat(produto.isAtivo()).isTrue();
        assertThat(produto.getImagens()).isEmpty();
    }

    @Test
    void deveRecusarPrecoZeroOuNegativo() {
        assertThatThrownBy(() -> Produto.novo("Mouse", null, BigDecimal.ZERO, 1, games))
                .isInstanceOf(DomainException.class)
                .hasMessage("O preço do produto deve ser maior que zero");

        assertThatThrownBy(() -> Produto.novo("Mouse", null, new BigDecimal("-1"), 1, games))
                .isInstanceOf(DomainException.class);
    }

    @Test
    void deveRecusarEstoqueNegativo() {
        assertThatThrownBy(() -> controleComEstoque(-1))
                .isInstanceOf(DomainException.class)
                .hasMessage("O estoque do produto não pode ser negativo");
    }

    @Test
    void deveRecusarProdutoSemCategoria() {
        assertThatThrownBy(() -> Produto.novo("Mouse", null, BigDecimal.TEN, 1, null))
                .isInstanceOf(DomainException.class)
                .hasMessage("A categoria do produto é obrigatória");
    }

    @Test
    void deveBaixarEstoque() {
        Produto produto = controleComEstoque(5);

        produto.baixarEstoque(3);

        assertThat(produto.getEstoque()).isEqualTo(2);
    }

    @Test
    void naoDeveBaixarMaisDoQueOEstoqueDisponivel() {
        Produto produto = controleComEstoque(2);

        assertThatThrownBy(() -> produto.baixarEstoque(3))
                .isInstanceOf(EstoqueInsuficienteException.class)
                .hasMessageContaining("disponível 2, solicitado 3");
        assertThat(produto.getEstoque()).isEqualTo(2);
    }

    @Test
    void deveReporEstoque() {
        Produto produto = controleComEstoque(0);

        produto.reporEstoque(4);

        assertThat(produto.getEstoque()).isEqualTo(4);
        assertThat(produto.temEstoque(4)).isTrue();
        assertThat(produto.temEstoque(5)).isFalse();
    }

    @Test
    void deveRecusarQuantidadeZeroAoMovimentarEstoque() {
        Produto produto = controleComEstoque(5);

        assertThatThrownBy(() -> produto.baixarEstoque(0)).isInstanceOf(DomainException.class);
        assertThatThrownBy(() -> produto.reporEstoque(-2)).isInstanceOf(DomainException.class);
    }

    @Test
    void soDeveEstarDisponivelParaVendaQuandoAtivoEComEstoque() {
        Produto produto = controleComEstoque(1);
        assertThat(produto.disponivelParaVenda()).isTrue();

        produto.desativar();
        assertThat(produto.disponivelParaVenda()).isFalse();

        produto.ativar();
        produto.baixarEstoque(1);
        assertThat(produto.disponivelParaVenda()).isFalse();
    }

    @Test
    void deveOrdenarImagensEUsarAPrimeiraComoPrincipal() {
        List<ImagemProduto> imagens = List.of(
                new ImagemProduto(2L, "/uploads/verso.jpg", 1),
                new ImagemProduto(1L, "/uploads/frente.jpg", 0));
        Produto produto = new Produto(7L, "Mouse Gamer Attack Shark X11", null, null,
                new BigDecimal("199.90"), 3, true, games, imagens);

        produto.adicionarImagem("/uploads/caixa.jpg");

        assertThat(produto.getImagens()).extracting(ImagemProduto::url)
                .containsExactly("/uploads/frente.jpg", "/uploads/verso.jpg", "/uploads/caixa.jpg");
        assertThat(produto.imagemPrincipal()).map(ImagemProduto::url).contains("/uploads/frente.jpg");
    }

    @Test
    void naoDevePermitirAlterarAListaDeImagensPorFora() {
        Produto produto = controleComEstoque(1);

        assertThatThrownBy(() -> produto.getImagens().add(new ImagemProduto(null, "/x.jpg", 0)))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
