package br.com.inovou.store.application.port.out;

import br.com.inovou.store.domain.produto.Categoria;

import java.util.List;

public interface CategoriaRepositoryPort {

    List<Categoria> listarTodas();
}
