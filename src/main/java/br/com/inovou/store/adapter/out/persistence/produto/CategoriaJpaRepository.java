package br.com.inovou.store.adapter.out.persistence.produto;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoriaJpaRepository extends JpaRepository<CategoriaJpaEntity, Long> {

    List<CategoriaJpaEntity> findAllByOrderByOrdemAscNomeAsc();
}
