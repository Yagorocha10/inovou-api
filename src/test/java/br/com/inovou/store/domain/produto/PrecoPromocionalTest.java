package br.com.inovou.store.domain.produto;

import br.com.inovou.store.domain.exception.DomainException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PrecoPromocionalTest {

    private final Categoria games = new Categoria(2L, "Games", "games");

    private Produto produto(String preco, String precoAnterior) {
        return new Produto(1L, "Controle", null, "", new BigDecimal(preco),
                precoAnterior == null ? null : new BigDecimal(precoAnterior), 3, true, games, List.of());
    }

    @Test
    void calculaOPercentualSemArredondarParaCima() {
        assertThat(produto("149.90", "199.90").percentualDesconto()).isEqualTo(25);
        assertThat(produto("66.67", "100.00").percentualDesconto()).isEqualTo(33);
        assertThat(produto("149.90", null).percentualDesconto()).isZero();
        assertThat(produto("149.90", null).emPromocao()).isFalse();
    }

    @Test
    void precoAnteriorPrecisaSerMaiorQueOPrecoAtual() {
        assertThatThrownBy(() -> produto("50.00", "50.00"))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("maior que o preço atual");
        assertThatThrownBy(() -> produto("50.00", "40.00")).isInstanceOf(DomainException.class);
        assertThatThrownBy(() -> produto("50.00", "60.001")).isInstanceOf(DomainException.class);
    }

    @Test
    void atualizarLigaEDesligaAPromocao() {
        Produto controle = produto("199.90", null);

        controle.atualizar("Controle", "", new BigDecimal("149.90"), new BigDecimal("199.90"), 3, games, true);
        assertThat(controle.emPromocao()).isTrue();
        assertThat(controle.getPrecoAnterior()).isEqualByComparingTo("199.90");

        controle.atualizar("Controle", "", new BigDecimal("149.90"), 3, games, true);
        assertThat(controle.emPromocao()).isFalse();
        assertThat(controle.getPrecoAnterior()).isNull();
    }

    @Test
    void naoDeixaOPrecoPassarDoPrecoAnterior() {
        Produto controle = produto("149.90", "199.90");

        assertThatThrownBy(() -> controle.alterarPreco(new BigDecimal("210.00"))).isInstanceOf(DomainException.class);
        assertThat(controle.getPreco()).isEqualByComparingTo("149.90");
    }
}
