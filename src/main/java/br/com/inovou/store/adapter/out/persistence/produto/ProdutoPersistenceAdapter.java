package br.com.inovou.store.adapter.out.persistence.produto;

import br.com.inovou.store.application.consulta.FiltroProdutos;
import br.com.inovou.store.application.consulta.OrdenacaoProduto;
import br.com.inovou.store.application.consulta.Pagina;
import br.com.inovou.store.application.consulta.Paginacao;
import br.com.inovou.store.application.port.out.ProdutoRepositoryPort;
import br.com.inovou.store.domain.produto.Produto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
@Transactional(readOnly = true)
public class ProdutoPersistenceAdapter implements ProdutoRepositoryPort {

    private static final Sort DESEMPATE = Sort.by(Sort.Direction.DESC, "id");

    private final ProdutoJpaRepository produtoJpaRepository;

    public ProdutoPersistenceAdapter(ProdutoJpaRepository produtoJpaRepository) {
        this.produtoJpaRepository = produtoJpaRepository;
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
