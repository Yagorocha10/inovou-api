package br.com.inovou.store.adapter.out.persistence.produto;

import br.com.inovou.store.application.consulta.OrdenacaoProduto;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;

import static org.assertj.core.api.Assertions.assertThat;

class ProdutoPersistenceAdapterTest {

    @Test
    void deveOrdenarPorPrecoComDesempatePorId() {
        Sort sort = ProdutoPersistenceAdapter.ordenarPor(OrdenacaoProduto.MENOR_PRECO);

        assertThat(sort.toList()).extracting(Sort.Order::getProperty, Sort.Order::getDirection)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple("preco", Sort.Direction.ASC),
                        org.assertj.core.groups.Tuple.tuple("id", Sort.Direction.DESC));
    }

    @Test
    void deveOrdenarMaisRecentesPrimeiro() {
        Sort sort = ProdutoPersistenceAdapter.ordenarPor(OrdenacaoProduto.MAIS_RECENTES);

        assertThat(sort.getOrderFor("criadoEm")).isNotNull();
        assertThat(sort.getOrderFor("criadoEm").getDirection()).isEqualTo(Sort.Direction.DESC);
    }
}
