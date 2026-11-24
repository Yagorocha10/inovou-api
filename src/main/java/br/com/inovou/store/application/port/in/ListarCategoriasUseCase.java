package br.com.inovou.store.application.port.in;

import br.com.inovou.store.domain.produto.Categoria;

import java.util.List;

public interface ListarCategoriasUseCase {

    List<Categoria> listar();
}
