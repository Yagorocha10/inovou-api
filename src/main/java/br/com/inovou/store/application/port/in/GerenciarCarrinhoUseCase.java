package br.com.inovou.store.application.port.in;

import br.com.inovou.store.domain.carrinho.Carrinho;

public interface GerenciarCarrinhoUseCase {

    Carrinho buscar(Long usuarioId);

    Carrinho adicionar(Long usuarioId, Long produtoId, int quantidade);

    Carrinho alterarQuantidade(Long usuarioId, Long produtoId, int quantidade);

    Carrinho remover(Long usuarioId, Long produtoId);

    Carrinho esvaziar(Long usuarioId);
}
