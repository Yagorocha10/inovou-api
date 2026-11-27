package br.com.inovou.store.application.port.in;

import br.com.inovou.store.application.consulta.AvaliacaoComProduto;
import br.com.inovou.store.application.consulta.AvaliacoesDoProduto;
import br.com.inovou.store.application.consulta.Paginacao;
import br.com.inovou.store.application.consulta.ResumoAvaliacoes;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface ConsultarAvaliacoesUseCase {

    AvaliacoesDoProduto doProduto(String slugProduto, Paginacao paginacao);

    Map<Long, ResumoAvaliacoes> resumosDosProdutos(Collection<Long> produtoIds);

    List<AvaliacaoComProduto> recentes(int limite);
}
