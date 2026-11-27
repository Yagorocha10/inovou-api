package br.com.inovou.store.adapter.out.persistence.pedido;

import br.com.inovou.store.domain.pedido.StatusPedido;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;

public interface PedidoJpaRepository extends JpaRepository<PedidoJpaEntity, Long> {

    Page<PedidoJpaEntity> findByUsuarioId(Long usuarioId, Pageable pageable);

    @Query("""
            select p from PedidoJpaEntity p
            where p.status in :status
              and (:busca = ''
                   or lower(p.codigo) like lower(concat('%', :busca, '%'))
                   or exists (select u.id from UsuarioJpaEntity u
                              where u.id = p.usuarioId
                                and (lower(u.nome) like lower(concat('%', :busca, '%'))
                                     or lower(u.email) like lower(concat('%', :busca, '%')))))
            """)
    Page<PedidoJpaEntity> buscarParaAdmin(@Param("status") Collection<StatusPedido> status,
                                          @Param("busca") String busca,
                                          Pageable pageable);

    long countByCriadoEmGreaterThanEqualAndCriadoEmLessThan(LocalDateTime inicio, LocalDateTime fim);

    long countByStatusIn(Collection<StatusPedido> status);

    @Query("""
            select sum(i.precoUnitario * i.quantidade) from ItemPedidoJpaEntity i
            where i.pedido.criadoEm >= :inicio and i.pedido.criadoEm < :fim and i.pedido.status <> :ignorado
            """)
    BigDecimal somarItensEntre(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim,
                               @Param("ignorado") StatusPedido ignorado);

    @Query("""
            select sum(p.frete) from PedidoJpaEntity p
            where p.criadoEm >= :inicio and p.criadoEm < :fim and p.status <> :ignorado
            """)
    BigDecimal somarFreteEntre(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim,
                               @Param("ignorado") StatusPedido ignorado);

    @Query("""
            select sum(p.desconto) from PedidoJpaEntity p
            where p.criadoEm >= :inicio and p.criadoEm < :fim and p.status <> :ignorado
            """)
    BigDecimal somarDescontoEntre(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim,
                                  @Param("ignorado") StatusPedido ignorado);

    @Query("""
            select count(i) from ItemPedidoJpaEntity i
            where i.produtoId = :produtoId and i.pedido.usuarioId = :usuarioId and i.pedido.status = :entregue
            """)
    long contarItensRecebidos(@Param("usuarioId") Long usuarioId, @Param("produtoId") Long produtoId,
                              @Param("entregue") StatusPedido entregue);
}
