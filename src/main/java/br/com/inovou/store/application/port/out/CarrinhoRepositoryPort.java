package br.com.inovou.store.application.port.out;

import br.com.inovou.store.domain.carrinho.Carrinho;

import java.util.Optional;

public interface CarrinhoRepositoryPort {

    Optional<Carrinho> buscarDoUsuario(Long usuarioId);

    Carrinho salvar(Carrinho carrinho);
}
