package br.com.inovou.store.application.service;

import br.com.inovou.store.application.port.in.GerenciarCarrinhoUseCase;
import br.com.inovou.store.application.port.out.CarrinhoRepositoryPort;
import br.com.inovou.store.application.port.out.ProdutoRepositoryPort;
import br.com.inovou.store.domain.carrinho.Carrinho;
import br.com.inovou.store.domain.exception.RecursoNaoEncontradoException;
import br.com.inovou.store.domain.produto.Produto;

import java.util.function.Consumer;

public class CarrinhoService implements GerenciarCarrinhoUseCase {

    private final CarrinhoRepositoryPort carrinhoRepository;
    private final ProdutoRepositoryPort produtoRepository;

    public CarrinhoService(CarrinhoRepositoryPort carrinhoRepository, ProdutoRepositoryPort produtoRepository) {
        this.carrinhoRepository = carrinhoRepository;
        this.produtoRepository = produtoRepository;
    }

    @Override
    public Carrinho buscar(Long usuarioId) {
        return carrinhoRepository.buscarDoUsuario(usuarioId).orElseGet(() -> Carrinho.vazio(usuarioId));
    }

    @Override
    public Carrinho adicionar(Long usuarioId, Long produtoId, int quantidade) {
        Produto produto = produtoRepository.buscarPorId(produtoId)
                .filter(Produto::isAtivo)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto não encontrado"));
        return alterar(usuarioId, carrinho -> carrinho.adicionar(produto, quantidade));
    }

    @Override
    public Carrinho alterarQuantidade(Long usuarioId, Long produtoId, int quantidade) {
        return alterar(usuarioId, carrinho -> carrinho.alterarQuantidade(produtoId, quantidade));
    }

    @Override
    public Carrinho remover(Long usuarioId, Long produtoId) {
        return alterar(usuarioId, carrinho -> carrinho.remover(produtoId));
    }

    @Override
    public Carrinho esvaziar(Long usuarioId) {
        return alterar(usuarioId, Carrinho::esvaziar);
    }

    private Carrinho alterar(Long usuarioId, Consumer<Carrinho> alteracao) {
        Carrinho carrinho = buscar(usuarioId);
        alteracao.accept(carrinho);
        return carrinhoRepository.salvar(carrinho);
    }
}
