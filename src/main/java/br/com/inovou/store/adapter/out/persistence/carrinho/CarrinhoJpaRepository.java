package br.com.inovou.store.adapter.out.persistence.carrinho;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CarrinhoJpaRepository extends JpaRepository<CarrinhoJpaEntity, Long> {

    Optional<CarrinhoJpaEntity> findByUsuarioId(Long usuarioId);
}
