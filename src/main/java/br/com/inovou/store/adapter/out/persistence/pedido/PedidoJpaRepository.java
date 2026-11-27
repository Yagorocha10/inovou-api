package br.com.inovou.store.adapter.out.persistence.pedido;

import br.com.inovou.store.domain.pedido.StatusPedido;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
}
