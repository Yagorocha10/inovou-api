package br.com.inovou.store.adapter.in.web.carrinho;

import br.com.inovou.store.domain.carrinho.Carrinho;
import br.com.inovou.store.domain.carrinho.ItemCarrinho;
import br.com.inovou.store.domain.produto.ImagemProduto;
import br.com.inovou.store.domain.produto.Produto;

import java.math.BigDecimal;
import java.util.List;

public record CarrinhoResponse(List<Item> itens, int quantidadeTotal, BigDecimal subtotal, boolean podeFinalizar) {

    public record Item(
            Long produtoId,
            String nome,
            String slug,
            String imagem,
            BigDecimal precoUnitario,
            int quantidade,
            BigDecimal subtotal,
            int estoqueDisponivel,
            boolean disponivel) {

        static Item de(ItemCarrinho item) {
            Produto produto = item.produto();
            return new Item(produto.getId(), produto.getNome(), produto.getSlug(),
                    produto.imagemPrincipal().map(ImagemProduto::url).orElse(null), produto.getPreco(),
                    item.quantidade(), item.subtotal(), produto.getEstoque(), item.podeSerComprado());
        }
    }

    public static CarrinhoResponse de(Carrinho carrinho) {
        return new CarrinhoResponse(carrinho.getItens().stream().map(Item::de).toList(),
                carrinho.quantidadeTotal(), carrinho.subtotal(), carrinho.podeFinalizar());
    }
}
