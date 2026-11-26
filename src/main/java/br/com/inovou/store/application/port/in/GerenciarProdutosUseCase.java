package br.com.inovou.store.application.port.in;

import br.com.inovou.store.application.consulta.DadosProduto;
import br.com.inovou.store.application.consulta.FiltroProdutosAdmin;
import br.com.inovou.store.application.consulta.Pagina;
import br.com.inovou.store.application.consulta.Paginacao;
import br.com.inovou.store.domain.produto.Produto;

public interface GerenciarProdutosUseCase {

    Pagina<Produto> listar(FiltroProdutosAdmin filtro, Paginacao paginacao);

    Produto buscar(Long id);

    Produto criar(DadosProduto dados);

    Produto atualizar(Long id, DadosProduto dados);

    Produto alterarAtivo(Long id, boolean ativo);
}
