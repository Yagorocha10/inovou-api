package br.com.inovou.store.application.port.out;

import br.com.inovou.store.application.consulta.FiltroProdutos;
import br.com.inovou.store.application.consulta.Pagina;
import br.com.inovou.store.application.consulta.Paginacao;
import br.com.inovou.store.domain.produto.Produto;

import java.util.List;
import java.util.Optional;

public interface ProdutoRepositoryPort {

    Pagina<Produto> buscarAtivos(FiltroProdutos filtro, Paginacao paginacao);

    Optional<Produto> buscarAtivoPorSlug(String slug);

    Optional<Produto> buscarPorId(Long id);

    List<Produto> buscarDisponiveisMaisRecentes(int limite);
}
