package br.com.inovou.store.application.port.out;

import br.com.inovou.store.application.consulta.AvaliacaoComProduto;
import br.com.inovou.store.application.consulta.Pagina;
import br.com.inovou.store.application.consulta.Paginacao;
import br.com.inovou.store.application.consulta.ResumoAvaliacoes;
import br.com.inovou.store.domain.avaliacao.Avaliacao;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface AvaliacaoRepositoryPort {

    Avaliacao salvar(Avaliacao avaliacao);

    boolean existe(Long produtoId, Long usuarioId);

    Pagina<Avaliacao> listarDoProduto(Long produtoId, Paginacao paginacao);

    Map<Long, ResumoAvaliacoes> resumosDosProdutos(Collection<Long> produtoIds);

    List<AvaliacaoComProduto> recentesComComentario(int limite);
}
