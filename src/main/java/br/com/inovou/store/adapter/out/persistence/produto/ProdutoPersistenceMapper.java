package br.com.inovou.store.adapter.out.persistence.produto;

import br.com.inovou.store.domain.produto.Categoria;
import br.com.inovou.store.domain.produto.ImagemProduto;
import br.com.inovou.store.domain.produto.Produto;

public final class ProdutoPersistenceMapper {

    private ProdutoPersistenceMapper() {
    }

    public static Categoria paraDominio(CategoriaJpaEntity entidade) {
        return new Categoria(entidade.getId(), entidade.getNome(), entidade.getSlug());
    }

    public static Produto paraDominio(ProdutoJpaEntity entidade) {
        return new Produto(
                entidade.getId(),
                entidade.getNome(),
                entidade.getSlug(),
                entidade.getDescricao(),
                entidade.getPreco(),
                entidade.getEstoque(),
                entidade.isAtivo(),
                paraDominio(entidade.getCategoria()),
                entidade.getImagens().stream()
                        .map(imagem -> new ImagemProduto(imagem.getId(), imagem.getUrl(), imagem.getOrdem()))
                        .toList());
    }
}
