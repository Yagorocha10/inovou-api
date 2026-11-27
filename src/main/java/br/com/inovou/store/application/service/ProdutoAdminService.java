package br.com.inovou.store.application.service;

import br.com.inovou.store.application.consulta.ArquivoImagem;
import br.com.inovou.store.application.consulta.DadosProduto;
import br.com.inovou.store.application.consulta.FiltroProdutosAdmin;
import br.com.inovou.store.application.consulta.Pagina;
import br.com.inovou.store.application.consulta.Paginacao;
import br.com.inovou.store.application.port.in.GerenciarImagensProdutoUseCase;
import br.com.inovou.store.application.port.in.GerenciarProdutosUseCase;
import br.com.inovou.store.application.port.out.ArmazenamentoImagemPort;
import br.com.inovou.store.application.port.out.CategoriaRepositoryPort;
import br.com.inovou.store.application.port.out.ProdutoRepositoryPort;
import br.com.inovou.store.domain.exception.ProdutoDuplicadoException;
import br.com.inovou.store.domain.exception.RecursoNaoEncontradoException;
import br.com.inovou.store.domain.produto.Categoria;
import br.com.inovou.store.domain.produto.ImagemProduto;
import br.com.inovou.store.domain.produto.Produto;

import java.util.List;

public class ProdutoAdminService implements GerenciarProdutosUseCase, GerenciarImagensProdutoUseCase {

    private final ProdutoRepositoryPort produtoRepository;
    private final CategoriaRepositoryPort categoriaRepository;
    private final ArmazenamentoImagemPort armazenamento;

    public ProdutoAdminService(ProdutoRepositoryPort produtoRepository, CategoriaRepositoryPort categoriaRepository,
                               ArmazenamentoImagemPort armazenamento) {
        this.produtoRepository = produtoRepository;
        this.categoriaRepository = categoriaRepository;
        this.armazenamento = armazenamento;
    }

    @Override
    public Pagina<Produto> listar(FiltroProdutosAdmin filtro, Paginacao paginacao) {
        return produtoRepository.buscarParaAdmin(filtro, paginacao);
    }

    @Override
    public Produto buscar(Long id) {
        return produtoRepository.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto não encontrado"));
    }

    @Override
    public Produto criar(DadosProduto dados) {
        Categoria categoria = categoria(dados.categoriaId());
        Produto produto = new Produto(null, dados.nome(), null, dados.descricao(), dados.preco(), dados.estoque(),
                dados.ativo(), categoria, List.of());
        garantirNomeUnico(produto.getNome(), null);
        return produtoRepository.salvar(produto);
    }

    @Override
    public Produto atualizar(Long id, DadosProduto dados) {
        Produto produto = buscar(id);
        Categoria categoria = categoria(dados.categoriaId());
        produto.atualizar(dados.nome(), dados.descricao(), dados.preco(), dados.estoque(), categoria, dados.ativo());
        garantirNomeUnico(produto.getNome(), id);
        return produtoRepository.salvar(produto);
    }

    @Override
    public Produto alterarAtivo(Long id, boolean ativo) {
        Produto produto = buscar(id);
        if (ativo) {
            produto.ativar();
        } else {
            produto.desativar();
        }
        return produtoRepository.salvar(produto);
    }

    @Override
    public Produto adicionar(Long produtoId, ArquivoImagem arquivo) {
        Produto produto = buscar(produtoId);
        String url = armazenamento.salvar(produtoId, arquivo);
        try {
            produto.adicionarImagem(url);
            return produtoRepository.salvar(produto);
        } catch (RuntimeException falha) {
            armazenamento.remover(url);
            throw falha;
        }
    }

    @Override
    public Produto remover(Long produtoId, Long imagemId) {
        Produto produto = buscar(produtoId);
        ImagemProduto removida = produto.removerImagem(imagemId);
        Produto salvo = produtoRepository.salvar(produto);
        armazenamento.remover(removida.url());
        return salvo;
    }

    @Override
    public Produto reordenar(Long produtoId, List<Long> idsNaNovaOrdem) {
        Produto produto = buscar(produtoId);
        produto.reordenarImagens(idsNaNovaOrdem);
        return produtoRepository.salvar(produto);
    }

    private Categoria categoria(Long categoriaId) {
        if (categoriaId == null) {
            throw new RecursoNaoEncontradoException("Categoria não encontrada");
        }
        return categoriaRepository.buscarPorId(categoriaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Categoria não encontrada"));
    }

    private void garantirNomeUnico(String nome, Long ignorarId) {
        if (produtoRepository.existeComNome(nome, ignorarId)) {
            throw new ProdutoDuplicadoException(nome);
        }
    }
}
