package br.com.inovou.store.adapter.out.persistence.usuario;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EnderecoJpaRepository extends JpaRepository<EnderecoJpaEntity, Long> {

    List<EnderecoJpaEntity> findByUsuarioIdOrderByIdAsc(Long usuarioId);

    Optional<EnderecoJpaEntity> findByIdAndUsuarioId(Long id, Long usuarioId);
}
