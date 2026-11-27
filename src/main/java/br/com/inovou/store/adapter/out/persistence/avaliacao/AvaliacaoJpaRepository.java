package br.com.inovou.store.adapter.out.persistence.avaliacao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface AvaliacaoJpaRepository extends JpaRepository<AvaliacaoJpaEntity, Long> {

    boolean existsByProdutoIdAndUsuarioId(Long produtoId, Long usuarioId);

    Page<AvaliacaoJpaEntity> findByProdutoId(Long produtoId, Pageable pageable);

    @Query("""
            select a.produtoId, avg(a.nota), count(a) from AvaliacaoJpaEntity a
            where a.produtoId in :produtoIds
            group by a.produtoId
            """)
    List<Object[]> resumirPorProduto(@Param("produtoIds") Collection<Long> produtoIds);

    @Query("""
            select a, p.nome, p.slug from AvaliacaoJpaEntity a, ProdutoJpaEntity p
            where p.id = a.produtoId
              and p.ativo = true
              and a.comentario is not null
            order by a.criadoEm desc, a.id desc
            """)
    List<Object[]> buscarRecentesComComentario(Pageable pageable);
}
