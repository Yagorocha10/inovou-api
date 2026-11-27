package br.com.inovou.store.domain.pedido;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class DescontoPixTest {

    @Test
    void descontoDeCincoPorCentoArredondadoNosCentavos() {
        assertThat(DescontoPix.sobre(new BigDecimal("289.80"))).isEqualByComparingTo("14.49");
        assertThat(DescontoPix.sobre(new BigDecimal("80.00"))).isEqualByComparingTo("4.00");
        assertThat(DescontoPix.sobre(BigDecimal.ZERO)).isEqualByComparingTo("0");
    }

    @Test
    void precoNoPixJaComODesconto() {
        assertThat(DescontoPix.precoNoPix(new BigDecimal("149.90"))).isEqualByComparingTo("142.40");
        assertThat(DescontoPix.precoNoPix(new BigDecimal("40.00"))).isEqualByComparingTo("38.00");
    }
}
