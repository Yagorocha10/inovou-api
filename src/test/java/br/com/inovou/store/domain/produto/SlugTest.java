package br.com.inovou.store.domain.produto;

import br.com.inovou.store.domain.exception.DomainException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SlugTest {

    @Test
    void deveRemoverAcentosEspacosESimbolos() {
        String slug = Slug.de("  Controle PS4 sem fio (Bluetooth) – Edição Ação!  ");

        assertThat(slug).isEqualTo("controle-ps4-sem-fio-bluetooth-edicao-acao");
    }

    @Test
    void deveManterNumerosEUnirHifensRepetidos() {
        String slug = Slug.de("Cabo USB-C para Lightning 20W -- 1m");

        assertThat(slug).isEqualTo("cabo-usb-c-para-lightning-20w-1m");
    }

    @Test
    void deveRecusarTextoVazio() {
        assertThatThrownBy(() -> Slug.de("   "))
                .isInstanceOf(DomainException.class);
    }

    @Test
    void deveRecusarTextoSemLetrasNemNumeros() {
        assertThatThrownBy(() -> Slug.de("!!! ???"))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("não gera um slug válido");
    }
}
