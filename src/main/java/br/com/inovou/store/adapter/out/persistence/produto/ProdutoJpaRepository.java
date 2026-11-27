package br.com.inovou.store.adapter.out.persistence.produto;

import br.com.inovou.store.domain.pedido.StatusPedido;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProdutoJpaRepository extends JpaRepository<ProdutoJpaEntity, Long> {

    @Query("""
            select p from ProdutoJpaEntity p
            join p.categoria c
            where p.ativo = true
              and (:busca = '' or lower(p.nome) like lower(concat('%', :busca, '%')))
              and (:categoria = '' or c.slug = :categoria)
              and (:apenasOfertas = false or p.precoAnterior is not null)
            """)
    Page<ProdutoJpaEntity> buscarAtivos(@Param("busca") String busca,
                                        @Param("categoria") String categoria,
                                        @Param("apenasOfertas") boolean apenasOfertas,
                                        Pageable pageable);

    @Query("""
            select i.produtoId from ItemPedidoJpaEntity i
            where i.pedido.status <> :ignorado
              and i.produtoId in (select p.id from ProdutoJpaEntity p where p.ativo = true and p.estoque > 0)
            group by i.produtoId
            order by sum(i.quantidade) desc, i.produtoId desc
            """)
    List<Long> buscarIdsMaisVendidos(@Param("ignorado") StatusPedido ignorado, Pageable pageable);

    Optional<ProdutoJpaEntity> findBySlugAndAtivoTrue(String slug);

    List<ProdutoJpaEntity> findByAtivoTrueAndEstoqueGreaterThan(int estoque, Pageable pageable);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update ProdutoJpaEntity p set p.estoque = p.estoque - :quantidade where p.id = :id and p.estoque >= :quantidade")
    int baixarEstoque(@Param("id") Long id, @Param("quantidade") int quantidade);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update ProdutoJpaEntity p set p.estoque = p.estoque + :quantidade where p.id = :id")
    int reporEstoque(@Param("id") Long id, @Param("quantidade") int quantidade);

    @Query("""
            select p from ProdutoJpaEntity p
            join p.categoria c
            where (:busca = '' or lower(p.nome) like lower(concat('%', :busca, '%')))
              and (:categoria = '' or c.slug = :categoria)
              and (:situacao = 'TODOS'
                   or (:situacao = 'ATIVOS' and p.ativo = true)
                   or (:situacao = 'INATIVOS' and p.ativo = false))
            """)
    Page<ProdutoJpaEntity> buscarParaAdmin(@Param("busca") String busca,
                                           @Param("categoria") String categoria,
                                           @Param("situacao") String situacao,
                                           Pageable pageable);

    boolean existsByNomeIgnoreCase(String nome);

    boolean existsByNomeIgnoreCaseAndIdNot(String nome, Long id);

    boolean existsBySlug(String slug);

    long countByAtivoTrueAndEstoque(int estoque);
}
