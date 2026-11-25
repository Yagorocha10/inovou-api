package br.com.inovou.store.domain.produto;

import br.com.inovou.store.domain.exception.DomainException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CategoriaTest {

    @Test
    void deveGerarSlugAPartirDoNome() {
        Categoria categoria = Categoria.nova(" Games e Controles ");

        assertThat(categoria.nome()).isEqualTo("Games e Controles");
        assertThat(categoria.slug()).isEqualTo("games-e-controles");
        assertThat(categoria.id()).isNull();
    }

    @Test
    void deveManterSlugInformado() {
        Categoria categoria = new Categoria(1L, "Eletrônicos", "eletronicos");

        assertThat(categoria.slug()).isEqualTo("eletronicos");
    }

    @Test
    void deveRecusarNomeVazio() {
        assertThatThrownBy(() -> Categoria.nova(""))
                .isInstanceOf(DomainException.class)
                .hasMessage("O nome da categoria é obrigatório");
    }
}
