package br.com.inovou.store.domain.avaliacao;

import br.com.inovou.store.domain.exception.DomainException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AvaliacaoTest {

    private static final LocalDateTime AGORA = LocalDateTime.of(2026, 11, 27, 10, 0);

    @Test
    void mostraSoOPrimeiroNomeEAInicialDoSobrenome() {
        Avaliacao avaliacao = Avaliacao.nova(1L, 2L, "Yago da Silva Rocha", 5, "  Chegou rápido!  ", AGORA);

        assertThat(avaliacao.autor()).isEqualTo("Yago R.");
        assertThat(avaliacao.comentario()).isEqualTo("Chegou rápido!");
        assertThat(avaliacao.criadoEm()).isEqualTo(AGORA);
    }

    @Test
    void nomeDeUmaPalavraFicaComoEsta() {
        assertThat(Avaliacao.nomePublico("  Mariana ")).isEqualTo("Mariana");
    }

    @Test
    void comentarioEmBrancoViraAvaliacaoSemComentario() {
        assertThat(Avaliacao.nova(1L, 2L, "Ana Souza", 4, "   ", AGORA).comentario()).isNull();
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 0, 6})
    void notaPrecisaSerDeUmACinco(int nota) {
        assertThatThrownBy(() -> Avaliacao.nova(1L, 2L, "Ana", nota, null, AGORA))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("1 a 5");
    }

    @Test
    void comentarioTemLimiteDeMilCaracteres() {
        assertThatThrownBy(() -> Avaliacao.nova(1L, 2L, "Ana", 5, "a".repeat(1001), AGORA))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("1000");
    }
}
