package br.com.inovou.store.domain.carrinho;

import br.com.inovou.store.domain.exception.DomainException;
import br.com.inovou.store.domain.exception.EstoqueInsuficienteException;
import br.com.inovou.store.domain.exception.RecursoNaoEncontradoException;
import br.com.inovou.store.domain.produto.Categoria;
import br.com.inovou.store.domain.produto.Produto;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CarrinhoTest {

    private final Categoria games = new Categoria(3L, "Games e controles", null);

    private Produto produto(long id, String preco, int estoque, boolean ativo) {
        return new Produto(id, "Produto " + id, null, null, new BigDecimal(preco), estoque, ativo, games, List.of());
    }

    @Test
    void deveSomarQuantidadeQuandoOMesmoProdutoForAdicionadoDeNovo() {
        Carrinho carrinho = Carrinho.vazio(1L);
        Produto controle = produto(10, "129.90", 5, true);

        carrinho.adicionar(controle, 2);
        carrinho.adicionar(controle, 1);

        assertThat(carrinho.getItens()).hasSize(1);
        assertThat(carrinho.getItens().getFirst().quantidade()).isEqualTo(3);
        assertThat(carrinho.quantidadeTotal()).isEqualTo(3);
    }

    @Test
    void deveCalcularSubtotal() {
        Carrinho carrinho = Carrinho.vazio(1L);
        carrinho.adicionar(produto(10, "129.90", 5, true), 2);
        carrinho.adicionar(produto(11, "30.00", 5, true), 1);

        assertThat(carrinho.subtotal()).isEqualByComparingTo("289.80");
        assertThat(carrinho.podeFinalizar()).isTrue();
    }

    @Test
    void naoDevePassarDoEstoqueNemSomandoComOQueJaEstaNoCarrinho() {
        Carrinho carrinho = Carrinho.vazio(1L);
        Produto console = produto(12, "40.00", 5, true);
        carrinho.adicionar(console, 4);

        assertThatThrownBy(() -> carrinho.adicionar(console, 2))
                .isInstanceOf(EstoqueInsuficienteException.class)
                .hasMessageContaining("disponível 5, solicitado 6");
        assertThat(carrinho.quantidadeTotal()).isEqualTo(4);
    }

    @Test
    void naoDeveAdicionarProdutoEsgotadoOuInativo() {
        Carrinho carrinho = Carrinho.vazio(1L);

        assertThatThrownBy(() -> carrinho.adicionar(produto(13, "50.00", 0, true), 1))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("não está disponível");
        assertThatThrownBy(() -> carrinho.adicionar(produto(14, "50.00", 9, false), 1))
                .isInstanceOf(DomainException.class);
        assertThat(carrinho.estaVazio()).isTrue();
    }

    @Test
    void deveAlterarQuantidadeRespeitandoOEstoque() {
        Carrinho carrinho = Carrinho.vazio(1L);
        carrinho.adicionar(produto(10, "10.00", 5, true), 1);

        carrinho.alterarQuantidade(10L, 5);
        assertThat(carrinho.quantidadeTotal()).isEqualTo(5);

        assertThatThrownBy(() -> carrinho.alterarQuantidade(10L, 6)).isInstanceOf(EstoqueInsuficienteException.class);
        assertThatThrownBy(() -> carrinho.alterarQuantidade(10L, 0)).isInstanceOf(DomainException.class);
        assertThatThrownBy(() -> carrinho.alterarQuantidade(99L, 1)).isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    void deveRemoverEEsvaziar() {
        Carrinho carrinho = Carrinho.vazio(1L);
        carrinho.adicionar(produto(10, "10.00", 5, true), 1);
        carrinho.adicionar(produto(11, "10.00", 5, true), 1);

        carrinho.remover(10L);
        assertThat(carrinho.getItens()).extracting(ItemCarrinho::produtoId).containsExactly(11L);

        carrinho.esvaziar();
        assertThat(carrinho.estaVazio()).isTrue();
        assertThat(carrinho.podeFinalizar()).isFalse();
        assertThatThrownBy(() -> carrinho.remover(11L)).isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    void itemSemEstoqueSuficienteImpedeFinalizar() {
        Produto quaseEsgotado = produto(10, "10.00", 1, true);
        Carrinho carrinho = new Carrinho(5L, 1L, List.of(new ItemCarrinho(quaseEsgotado, 3)));

        assertThat(carrinho.getItens().getFirst().podeSerComprado()).isFalse();
        assertThat(carrinho.podeFinalizar()).isFalse();
    }
}
