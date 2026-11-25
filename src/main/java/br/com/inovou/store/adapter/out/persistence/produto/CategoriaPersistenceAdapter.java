package br.com.inovou.store.adapter.out.persistence.produto;

import br.com.inovou.store.application.port.out.CategoriaRepositoryPort;
import br.com.inovou.store.domain.produto.Categoria;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@Transactional(readOnly = true)
public class CategoriaPersistenceAdapter implements CategoriaRepositoryPort {

    private final CategoriaJpaRepository categoriaJpaRepository;

    public CategoriaPersistenceAdapter(CategoriaJpaRepository categoriaJpaRepository) {
        this.categoriaJpaRepository = categoriaJpaRepository;
    }

    @Override
    public List<Categoria> listarTodas() {
        return categoriaJpaRepository.findAllByOrderByNomeAsc().stream()
                .map(ProdutoPersistenceMapper::paraDominio)
                .toList();
    }
}
