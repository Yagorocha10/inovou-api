package br.com.inovou.store.adapter.in.web.categoria;

import br.com.inovou.store.application.port.in.ListarCategoriasUseCase;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {

    private final ListarCategoriasUseCase listarCategorias;

    public CategoriaController(ListarCategoriasUseCase listarCategorias) {
        this.listarCategorias = listarCategorias;
    }

    @GetMapping
    public List<CategoriaResponse> listar() {
        return listarCategorias.listar().stream().map(CategoriaResponse::de).toList();
    }
}
