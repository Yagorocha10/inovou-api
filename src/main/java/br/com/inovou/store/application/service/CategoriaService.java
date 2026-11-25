package br.com.inovou.store.application.service;

import br.com.inovou.store.application.port.in.ListarCategoriasUseCase;
import br.com.inovou.store.application.port.out.CategoriaRepositoryPort;
import br.com.inovou.store.domain.produto.Categoria;

import java.util.List;

public class CategoriaService implements ListarCategoriasUseCase {

    private final CategoriaRepositoryPort categoriaRepository;

    public CategoriaService(CategoriaRepositoryPort categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    @Override
    public List<Categoria> listar() {
        return categoriaRepository.listarTodas();
    }
}
