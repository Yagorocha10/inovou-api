package br.com.inovou.store.domain.usuario;

import br.com.inovou.store.domain.exception.DomainException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UsuarioTest {

    @Test
    void deveNormalizarEmailENome() {
        Usuario usuario = Usuario.novoCliente("  Yago   da Silva ", " Yago@Exemplo.COM ", "hash");

        assertThat(usuario.getEmail()).isEqualTo("yago@exemplo.com");
        assertThat(usuario.getNome()).isEqualTo("Yago da Silva");
        assertThat(usuario.getRole()).isEqualTo(Role.CLIENTE);
        assertThat(usuario.ehAdmin()).isFalse();
    }

    @Test
    void deveCriarAdmin() {
        assertThat(Usuario.novoAdmin("Admin", "admin@inovou.com", "hash").ehAdmin()).isTrue();
    }

    @Test
    void deveRecusarEmailInvalido() {
        assertThatThrownBy(() -> Usuario.novoCliente("Yago", "yago.sem.arroba", "hash"))
                .isInstanceOf(DomainException.class)
                .hasMessage("E-mail inválido");
    }

    @Test
    void deveRecusarNomeCurtoOuSemSenha() {
        assertThatThrownBy(() -> Usuario.novoCliente("Y", "y@x.com", "hash")).isInstanceOf(DomainException.class);
        assertThatThrownBy(() -> Usuario.novoCliente("Yago", "y@x.com", " ")).isInstanceOf(DomainException.class);
    }
}
