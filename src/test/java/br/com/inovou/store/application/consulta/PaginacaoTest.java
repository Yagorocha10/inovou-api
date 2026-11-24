package br.com.inovou.store.application.consulta;

import br.com.inovou.store.domain.exception.DomainException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PaginacaoTest {

    @Test
    void deveUsarValoresPadraoQuandoNaoInformados() {
        Paginacao paginacao = Paginacao.de(null, null);

        assertThat(paginacao.pagina()).isZero();
        assertThat(paginacao.tamanho()).isEqualTo(Paginacao.TAMANHO_PADRAO);
    }

    @Test
    void deveRecusarPaginaNegativaETamanhoForaDoLimite() {
        assertThatThrownBy(() -> new Paginacao(-1, 12)).isInstanceOf(DomainException.class);
        assertThatThrownBy(() -> new Paginacao(0, 0)).isInstanceOf(DomainException.class);
        assertThatThrownBy(() -> new Paginacao(0, Paginacao.TAMANHO_MAXIMO + 1)).isInstanceOf(DomainException.class);
    }

    @Test
    void deveCalcularTotalDePaginasArredondandoParaCima() {
        Pagina<String> pagina = new Pagina<>(List.of("a", "b"), 0, 12, 25);

        assertThat(pagina.totalPaginas()).isEqualTo(3);
    }

    @Test
    void filtroDeveTrocarNulosPorValoresNeutros() {
        FiltroProdutos filtro = new FiltroProdutos(null, "  games-e-controles ", null);

        assertThat(filtro.busca()).isEmpty();
        assertThat(filtro.categoria()).isEqualTo("games-e-controles");
        assertThat(filtro.ordenacao()).isEqualTo(OrdenacaoProduto.MAIS_RECENTES);
    }
}
