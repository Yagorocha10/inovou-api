package br.com.inovou.store.domain.produto;

import br.com.inovou.store.domain.exception.DomainException;

public record ImagemProduto(Long id, String url, int ordem) {

    public ImagemProduto {
        if (url == null || url.isBlank()) {
            throw new DomainException("A URL da imagem é obrigatória");
        }
        if (ordem < 0) {
            throw new DomainException("A ordem da imagem não pode ser negativa");
        }
    }
}
