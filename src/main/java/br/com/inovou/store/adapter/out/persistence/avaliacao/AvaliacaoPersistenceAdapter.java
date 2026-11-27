package br.com.inovou.store.adapter.out.persistence.avaliacao;

import br.com.inovou.store.application.consulta.AvaliacaoComProduto;
import br.com.inovou.store.application.consulta.Pagina;
import br.com.inovou.store.application.consulta.Paginacao;
import br.com.inovou.store.application.consulta.ResumoAvaliacoes;
import br.com.inovou.store.application.port.out.AvaliacaoRepositoryPort;
import br.com.inovou.store.domain.avaliacao.Avaliacao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Transactional(readOnly = true)
public class AvaliacaoPersistenceAdapter implements AvaliacaoRepositoryPort {

    private static final Sort MAIS_RECENTES_PRIMEIRO = Sort.by(Sort.Direction.DESC, "criadoEm", "id");

    private final AvaliacaoJpaRepository avaliacaoJpaRepository;

    public AvaliacaoPersistenceAdapter(AvaliacaoJpaRepository avaliacaoJpaRepository) {
        this.avaliacaoJpaRepository = avaliacaoJpaRepository;
    }

    @Override
    @Transactional
    public Avaliacao salvar(Avaliacao avaliacao) {
        return paraDominio(avaliacaoJpaRepository.saveAndFlush(new AvaliacaoJpaEntity(avaliacao)));
    }

    @Override
    public boolean existe(Long produtoId, Long usuarioId) {
        return avaliacaoJpaRepository.existsByProdutoIdAndUsuarioId(produtoId, usuarioId);
    }

    @Override
    public Pagina<Avaliacao> listarDoProduto(Long produtoId, Paginacao paginacao) {
        Page<AvaliacaoJpaEntity> pagina = avaliacaoJpaRepository.findByProdutoId(produtoId,
                PageRequest.of(paginacao.pagina(), paginacao.tamanho(), MAIS_RECENTES_PRIMEIRO));
        return new Pagina<>(pagina.getContent().stream().map(AvaliacaoPersistenceAdapter::paraDominio).toList(),
                paginacao.pagina(), paginacao.tamanho(), pagina.getTotalElements());
    }

    @Override
    public Map<Long, ResumoAvaliacoes> resumosDosProdutos(Collection<Long> produtoIds) {
        if (produtoIds == null || produtoIds.isEmpty()) {
            return Map.of();
        }
        Map<Long, ResumoAvaliacoes> resumos = new HashMap<>();
        for (Object[] linha : avaliacaoJpaRepository.resumirPorProduto(produtoIds)) {
            Long produtoId = ((Number) linha[0]).longValue();
            BigDecimal media = BigDecimal.valueOf(((Number) linha[1]).doubleValue());
            long total = ((Number) linha[2]).longValue();
            resumos.put(produtoId, new ResumoAvaliacoes(media, total));
        }
        return resumos;
    }

    @Override
    public List<AvaliacaoComProduto> recentesComComentario(int limite) {
        return avaliacaoJpaRepository.buscarRecentesComComentario(PageRequest.of(0, limite)).stream()
                .map(linha -> new AvaliacaoComProduto(paraDominio((AvaliacaoJpaEntity) linha[0]),
                        (String) linha[1], (String) linha[2]))
                .toList();
    }

    private static Avaliacao paraDominio(AvaliacaoJpaEntity entidade) {
        return new Avaliacao(entidade.getId(), entidade.getProdutoId(), entidade.getUsuarioId(),
                entidade.getAutor(), entidade.getNota(), entidade.getComentario(), entidade.getCriadoEm());
    }
}
