package br.com.inovou.store.adapter.in.web.categoria;

import br.com.inovou.store.domain.produto.Categoria;

public record CategoriaResponse(Long id, String nome, String slug) {

    public static CategoriaResponse de(Categoria categoria) {
        return new CategoriaResponse(categoria.id(), categoria.nome(), categoria.slug());
    }
}
