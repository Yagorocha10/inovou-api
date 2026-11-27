package br.com.inovou.store.application.port.out;

import br.com.inovou.store.domain.produto.Categoria;

import java.util.List;
import java.util.Optional;

public interface CategoriaRepositoryPort {

    List<Categoria> listarTodas();

    Optional<Categoria> buscarPorId(Long id);
}
