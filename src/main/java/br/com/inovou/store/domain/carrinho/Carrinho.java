package br.com.inovou.store.domain.carrinho;

import br.com.inovou.store.domain.exception.DomainException;
import br.com.inovou.store.domain.exception.EstoqueInsuficienteException;
import br.com.inovou.store.domain.exception.RecursoNaoEncontradoException;
import br.com.inovou.store.domain.produto.Produto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class Carrinho {

    public static final int LIMITE_ITENS_DIFERENTES = 30;

    private final Long id;
    private final Long usuarioId;
    private final List<ItemCarrinho> itens;

    public Carrinho(Long id, Long usuarioId, List<ItemCarrinho> itens) {
        if (usuarioId == null) {
            throw new DomainException("O carrinho precisa pertencer a um usuário");
        }
        this.id = id;
        this.usuarioId = usuarioId;
        this.itens = new ArrayList<>(itens == null ? List.of() : itens);
    }

    public static Carrinho vazio(Long usuarioId) {
        return new Carrinho(null, usuarioId, List.of());
    }

    public void adicionar(Produto produto, int quantidade) {
        if (quantidade <= 0) {
            throw new DomainException("A quantidade deve ser maior que zero");
        }
        if (!produto.disponivelParaVenda()) {
            throw new DomainException("O produto %s não está disponível no momento".formatted(produto.getNome()));
        }
        Optional<ItemCarrinho> existente = buscarItem(produto.getId());
        if (existente.isEmpty() && itens.size() >= LIMITE_ITENS_DIFERENTES) {
            throw new DomainException("O carrinho pode ter no máximo %d produtos diferentes"
                    .formatted(LIMITE_ITENS_DIFERENTES));
        }
        int novaQuantidade = existente.map(ItemCarrinho::quantidade).orElse(0) + quantidade;
        garantirEstoque(produto, novaQuantidade);
        existente.ifPresentOrElse(
                item -> substituir(item, new ItemCarrinho(produto, novaQuantidade)),
                () -> itens.add(new ItemCarrinho(produto, novaQuantidade)));
    }

    public void alterarQuantidade(Long produtoId, int quantidade) {
        ItemCarrinho item = buscarItem(produtoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto não está no carrinho"));
        garantirEstoque(item.produto(), quantidade);
        substituir(item, item.comQuantidade(quantidade));
    }

    public void remover(Long produtoId) {
        boolean removido = itens.removeIf(item -> Objects.equals(item.produtoId(), produtoId));
        if (!removido) {
            throw new RecursoNaoEncontradoException("Produto não está no carrinho");
        }
    }

    public void esvaziar() {
        itens.clear();
    }

    public BigDecimal subtotal() {
        return itens.stream().map(ItemCarrinho::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public int quantidadeTotal() {
        return itens.stream().mapToInt(ItemCarrinho::quantidade).sum();
    }

    public boolean estaVazio() {
        return itens.isEmpty();
    }

    public boolean podeFinalizar() {
        return !itens.isEmpty() && itens.stream().allMatch(ItemCarrinho::podeSerComprado);
    }

    public Long getId() {
        return id;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public List<ItemCarrinho> getItens() {
        return List.copyOf(itens);
    }

    private Optional<ItemCarrinho> buscarItem(Long produtoId) {
        return itens.stream().filter(item -> Objects.equals(item.produtoId(), produtoId)).findFirst();
    }

    private void substituir(ItemCarrinho antigo, ItemCarrinho novo) {
        itens.set(itens.indexOf(antigo), novo);
    }

    private static void garantirEstoque(Produto produto, int quantidade) {
        if (quantidade <= 0) {
            throw new DomainException("A quantidade deve ser maior que zero");
        }
        if (!produto.temEstoque(quantidade)) {
            throw new EstoqueInsuficienteException(produto.getNome(), produto.getEstoque(), quantidade);
        }
    }
}
