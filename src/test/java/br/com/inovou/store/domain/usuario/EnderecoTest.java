package br.com.inovou.store.domain.usuario;

import br.com.inovou.store.domain.exception.DomainException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EnderecoTest {

    private Endereco endereco(String cep, String uf) {
        return new Endereco(null, 1L, cep, "Av. Nazaré", "100", " ", "Nazaré", "Belém", uf);
    }

    @Test
    void deveFormatarCepEUfENormalizarComplementoVazio() {
        Endereco endereco = endereco("66035170", "pa");

        assertThat(endereco.cep()).isEqualTo("66035-170");
        assertThat(endereco.uf()).isEqualTo("PA");
        assertThat(endereco.complemento()).isNull();
        assertThat(endereco.pertenceA(1L)).isTrue();
        assertThat(endereco.pertenceA(2L)).isFalse();
    }

    @Test
    void deveRecusarCepComTamanhoErrado() {
        assertThatThrownBy(() -> endereco("6603-517", "PA"))
                .isInstanceOf(DomainException.class)
                .hasMessage("O CEP deve ter 8 dígitos");
    }

    @Test
    void deveRecusarUfInexistente() {
        assertThatThrownBy(() -> endereco("66035-170", "XX"))
                .isInstanceOf(DomainException.class)
                .hasMessage("UF inválida");
    }

    @Test
    void deveExigirCamposObrigatorios() {
        assertThatThrownBy(() -> new Endereco(null, 1L, "66035-170", " ", "1", null, "Nazaré", "Belém", "PA"))
                .isInstanceOf(DomainException.class)
                .hasMessage("O campo logradouro é obrigatório");
    }
}
