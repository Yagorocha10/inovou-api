package br.com.inovou.store.adapter.out.persistence.produto;

import br.com.inovou.store.application.consulta.FiltroProdutos;
import br.com.inovou.store.application.consulta.FiltroProdutosAdmin;
import br.com.inovou.store.application.consulta.OrdenacaoProduto;
import br.com.inovou.store.application.consulta.Pagina;
import br.com.inovou.store.application.consulta.Paginacao;
import br.com.inovou.store.application.port.out.ProdutoRepositoryPort;
import br.com.inovou.store.domain.exception.RecursoNaoEncontradoException;
import br.com.inovou.store.domain.produto.ImagemProduto;
import br.com.inovou.store.domain.produto.Produto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Transactional(readOnly = true)
public class ProdutoPersistenceAdapter implements ProdutoRepositoryPort {

    private static final Sort DESEMPATE = Sort.by(Sort.Direction.DESC, "id");

    private final ProdutoJpaRepository produtoJpaRepository;
    private final CategoriaJpaRepository categoriaJpaRepository;

    public ProdutoPersistenceAdapter(ProdutoJpaRepository produtoJpaRepository,
                                     CategoriaJpaRepository categoriaJpaRepository) {
        this.produtoJpaRepository = produtoJpaRepository;
        this.categoriaJpaRepository = categoriaJpaRepository;
    }

    @Override
    public Pagina<Produto> buscarAtivos(FiltroProdutos filtro, Paginacao paginacao) {
        PageRequest pageRequest = PageRequest.of(paginacao.pagina(), paginacao.tamanho(), ordenarPor(filtro.ordenacao()));
        Page<ProdutoJpaEntity> pagina = produtoJpaRepository.buscarAtivos(filtro.busca(), filtro.categoria(), pageRequest);
        List<Produto> produtos = pagina.getContent().stream().map(ProdutoPersistenceMapper::paraDominio).toList();
        return new Pagina<>(produtos, paginacao.pagina(), paginacao.tamanho(), pagina.getTotalElements());
    }

    @Override
    public Optional<Produto> buscarAtivoPorSlug(String slug) {
        return produtoJpaRepository.findBySlugAndAtivoTrue(slug).map(ProdutoPersistenceMapper::paraDominio);
    }

    @Override
    public Optional<Produto> buscarPorId(Long id) {
        return produtoJpaRepository.findById(id).map(ProdutoPersistenceMapper::paraDominio);
    }

    @Override
    public List<Produto> buscarDisponiveisMaisRecentes(int limite) {
        PageRequest pageRequest = PageRequest.of(0, limite, ordenarPor(OrdenacaoProduto.MAIS_RECENTES));
        return produtoJpaRepository.findByAtivoTrueAndEstoqueGreaterThan(0, pageRequest).stream()
                .map(ProdutoPersistenceMapper::paraDominio)
                .toList();
    }

    @Override
    @Transactional
    public boolean baixarEstoque(Long produtoId, int quantidade) {
        return produtoJpaRepository.baixarEstoque(produtoId, quantidade) == 1;
    }

    @Override
    @Transactional
    public void reporEstoque(Long produtoId, int quantidade) {
        produtoJpaRepository.reporEstoque(produtoId, quantidade);
    }

    @Override
    public Pagina<Produto> buscarParaAdmin(FiltroProdutosAdmin filtro, Paginacao paginacao) {
        PageRequest pageRequest = PageRequest.of(paginacao.pagina(), paginacao.tamanho(),
                ordenarPor(OrdenacaoProduto.MAIS_RECENTES));
        Page<ProdutoJpaEntity> pagina = produtoJpaRepository.buscarParaAdmin(filtro.busca(), filtro.categoria(),
                filtro.situacao().name(), pageRequest);
        List<Produto> produtos = pagina.getContent().stream().map(ProdutoPersistenceMapper::paraDominio).toList();
        return new Pagina<>(produtos, paginacao.pagina(), paginacao.tamanho(), pagina.getTotalElements());
    }

    @Override
    public boolean existeComNome(String nome, Long ignorarId) {
        return ignorarId == null
                ? produtoJpaRepository.existsByNomeIgnoreCase(nome)
                : produtoJpaRepository.existsByNomeIgnoreCaseAndIdNot(nome, ignorarId);
    }

    @Override
    @Transactional
    public Produto salvar(Produto produto) {
        ProdutoJpaEntity entidade;
        if (produto.getId() == null) {
            entidade = new ProdutoJpaEntity();
            entidade.setSlug(slugDisponivel(produto.getSlug()));
        } else {
            entidade = produtoJpaRepository.findById(produto.getId())
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Produto não encontrado"));
        }
        entidade.setNome(produto.getNome());
        entidade.setDescricao(produto.getDescricao());
        entidade.setPreco(produto.getPreco());
        entidade.setEstoque(produto.getEstoque());
        entidade.setAtivo(produto.isAtivo());
        entidade.setCategoria(categoriaJpaRepository.getReferenceById(produto.getCategoria().id()));
        sincronizarImagens(entidade, produto.getImagens());
        ProdutoJpaEntity salva = produtoJpaRepository.saveAndFlush(entidade);
        salva.getImagens().sort(Comparator.comparingInt(ImagemProdutoJpaEntity::getOrdem));
        return ProdutoPersistenceMapper.paraDominio(salva);
    }

    private static void sincronizarImagens(ProdutoJpaEntity entidade, List<ImagemProduto> desejadas) {
        Map<Long, ImagemProduto> porId = desejadas.stream()
                .filter(imagem -> imagem.id() != null)
                .collect(Collectors.toMap(ImagemProduto::id, Function.identity()));
        entidade.getImagens().removeIf(existente -> !porId.containsKey(existente.getId()));
        for (ImagemProdutoJpaEntity existente : entidade.getImagens()) {
            existente.setOrdem(porId.get(existente.getId()).ordem());
        }
        for (ImagemProduto nova : desejadas) {
            if (nova.id() == null) {
                ImagemProdutoJpaEntity imagem = new ImagemProdutoJpaEntity();
                imagem.setProduto(entidade);
                imagem.setUrl(nova.url());
                imagem.setOrdem(nova.ordem());
                entidade.getImagens().add(imagem);
            }
        }
    }

    private String slugDisponivel(String base) {
        String candidato = base;
        int sufixo = 2;
        while (produtoJpaRepository.existsBySlug(candidato)) {
            candidato = base + "-" + sufixo++;
        }
        return candidato;
    }

    static Sort ordenarPor(OrdenacaoProduto ordenacao) {
        Sort criterio = switch (ordenacao) {
            case MAIS_RECENTES -> Sort.by(Sort.Direction.DESC, "criadoEm");
            case MENOR_PRECO -> Sort.by(Sort.Direction.ASC, "preco");
            case MAIOR_PRECO -> Sort.by(Sort.Direction.DESC, "preco");
            case NOME -> Sort.by(Sort.Direction.ASC, "nome");
        };
        return criterio.and(DESEMPATE);
    }
}
