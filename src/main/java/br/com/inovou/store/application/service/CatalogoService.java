package br.com.inovou.store.application.service;

import br.com.inovou.store.application.consulta.FiltroProdutos;
import br.com.inovou.store.application.consulta.Pagina;
import br.com.inovou.store.application.consulta.Paginacao;
import br.com.inovou.store.application.port.in.BuscarProdutosUseCase;
import br.com.inovou.store.application.port.in.DetalharProdutoUseCase;
import br.com.inovou.store.application.port.in.ListarDestaquesUseCase;
import br.com.inovou.store.application.port.out.ProdutoRepositoryPort;
import br.com.inovou.store.domain.exception.RecursoNaoEncontradoException;
import br.com.inovou.store.domain.produto.Produto;

import java.util.List;

public class CatalogoService implements BuscarProdutosUseCase, DetalharProdutoUseCase, ListarDestaquesUseCase {

    static final int LIMITE_MAXIMO_DESTAQUES = 24;

    private final ProdutoRepositoryPort produtoRepository;

    public CatalogoService(ProdutoRepositoryPort produtoRepository) {
        this.produtoRepository = produtoRepository;
    }

    @Override
    public Pagina<Produto> buscar(FiltroProdutos filtro, Paginacao paginacao) {
        return produtoRepository.buscarAtivos(filtro, paginacao);
    }

    @Override
    public Produto detalhar(String slug) {
        return produtoRepository.buscarAtivoPorSlug(slug)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto não encontrado: " + slug));
    }

    @Override
    public List<Produto> listarDestaques(int limite) {
        int limiteValido = Math.clamp(limite, 1, LIMITE_MAXIMO_DESTAQUES);
        return produtoRepository.buscarDisponiveisMaisRecentes(limiteValido);
    }

    @Override
    public List<Produto> listarMaisVendidos(int limite) {
        int limiteValido = Math.clamp(limite, 1, LIMITE_MAXIMO_DESTAQUES);
        return produtoRepository.buscarMaisVendidos(limiteValido);
    }
}
