package br.com.inovou.store.domain.produto;

import br.com.inovou.store.domain.exception.DomainException;

public record Categoria(Long id, String nome, String slug) {

    public Categoria {
        if (nome == null || nome.isBlank()) {
            throw new DomainException("O nome da categoria é obrigatório");
        }
        nome = nome.strip();
        slug = (slug == null || slug.isBlank()) ? Slug.de(nome) : slug;
    }

    public static Categoria nova(String nome) {
        return new Categoria(null, nome, null);
    }
}
