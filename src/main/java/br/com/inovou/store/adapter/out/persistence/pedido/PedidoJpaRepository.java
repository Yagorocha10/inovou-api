package br.com.inovou.store.adapter.out.persistence.pedido;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PedidoJpaRepository extends JpaRepository<PedidoJpaEntity, Long> {

    Page<PedidoJpaEntity> findByUsuarioId(Long usuarioId, Pageable pageable);
}
