package br.com.inovou.store.domain.frete;

import br.com.inovou.store.domain.exception.DomainException;
import br.com.inovou.store.domain.exception.EntregaIndisponivelException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TabelaFreteTest {

    @ParameterizedTest
    @ValueSource(strings = {"66000-000", "66010000", "66999-999"})
    void cepDeBelemPagaOFreteDeBelem(String cep) {
        Frete frete = TabelaFrete.calcular(cep);

        assertThat(frete.regiao()).isEqualTo("Belém");
        assertThat(frete.valor()).isEqualByComparingTo("10.00");
        assertThat(frete.prazoDias()).isEqualTo(2);
    }

    @ParameterizedTest
    @ValueSource(strings = {"67030-000", "67200-000", "68795-000"})
    void cepDaRegiaoMetropolitanaPagaOFreteDaRegiao(String cep) {
        assertThat(TabelaFrete.calcular(cep).valor()).isEqualByComparingTo("15.00");
    }

    @Test
    void cepForaDaAreaDeEntregaEhRecusado() {
        assertThatThrownBy(() -> TabelaFrete.calcular("01310-100"))
                .isInstanceOf(EntregaIndisponivelException.class)
                .hasMessageContaining("01310-100");
    }

    @Test
    void cepMalFormadoEhRecusado() {
        assertThatThrownBy(() -> TabelaFrete.calcular("6600")).isInstanceOf(DomainException.class);
    }
}
