package br.com.inovou.store.application.port.in;

import br.com.inovou.store.domain.produto.Produto;

public interface DetalharProdutoUseCase {

    Produto detalhar(String slug);
}
