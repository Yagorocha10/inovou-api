package br.com.inovou.store.application.port.in;

import br.com.inovou.store.application.consulta.FiltroProdutos;
import br.com.inovou.store.application.consulta.Pagina;
import br.com.inovou.store.application.consulta.Paginacao;
import br.com.inovou.store.domain.produto.Produto;

public interface BuscarProdutosUseCase {

    Pagina<Produto> buscar(FiltroProdutos filtro, Paginacao paginacao);
}
