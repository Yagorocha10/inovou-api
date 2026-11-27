package br.com.inovou.store.adapter.in.web.admin;

import br.com.inovou.store.adapter.in.web.categoria.CategoriaResponse;
import br.com.inovou.store.domain.produto.ImagemProduto;
import br.com.inovou.store.domain.produto.Produto;

import java.math.BigDecimal;
import java.util.List;

public record ProdutoAdminResponse(Long id, String nome, String slug, String descricao, BigDecimal preco,
                                   BigDecimal precoAnterior, int estoque, boolean ativo, CategoriaResponse categoria,
                                   List<ImagemAdminResponse> imagens) {

    public record ImagemAdminResponse(Long id, String url, int ordem) {

        static ImagemAdminResponse de(ImagemProduto imagem) {
            return new ImagemAdminResponse(imagem.id(), imagem.url(), imagem.ordem());
        }
    }

    static ProdutoAdminResponse de(Produto produto) {
        return new ProdutoAdminResponse(produto.getId(), produto.getNome(), produto.getSlug(),
                produto.getDescricao(), produto.getPreco(), produto.getPrecoAnterior(), produto.getEstoque(),
                produto.isAtivo(),
                CategoriaResponse.de(produto.getCategoria()),
                produto.getImagens().stream().map(ImagemAdminResponse::de).toList());
    }
}
