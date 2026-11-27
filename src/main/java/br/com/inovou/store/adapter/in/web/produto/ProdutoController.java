package br.com.inovou.store.adapter.in.web.produto;

import br.com.inovou.store.adapter.in.web.comum.PaginaResponse;
import br.com.inovou.store.application.consulta.FiltroProdutos;
import br.com.inovou.store.application.consulta.OrdenacaoProduto;
import br.com.inovou.store.application.consulta.Pagina;
import br.com.inovou.store.application.consulta.Paginacao;
import br.com.inovou.store.application.consulta.ResumoAvaliacoes;
import br.com.inovou.store.application.port.in.BuscarProdutosUseCase;
import br.com.inovou.store.application.port.in.ConsultarAvaliacoesUseCase;
import br.com.inovou.store.application.port.in.DetalharProdutoUseCase;
import br.com.inovou.store.application.port.in.ListarDestaquesUseCase;
import br.com.inovou.store.domain.produto.Produto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/produtos")
public class ProdutoController {

    private final BuscarProdutosUseCase buscarProdutos;
    private final DetalharProdutoUseCase detalharProduto;
    private final ListarDestaquesUseCase listarDestaques;
    private final ConsultarAvaliacoesUseCase consultarAvaliacoes;

    public ProdutoController(BuscarProdutosUseCase buscarProdutos,
                             DetalharProdutoUseCase detalharProduto,
                             ListarDestaquesUseCase listarDestaques,
                             ConsultarAvaliacoesUseCase consultarAvaliacoes) {
        this.buscarProdutos = buscarProdutos;
        this.detalharProduto = detalharProduto;
        this.listarDestaques = listarDestaques;
        this.consultarAvaliacoes = consultarAvaliacoes;
    }

    @GetMapping
    public PaginaResponse<ProdutoResumoResponse> buscar(
            @RequestParam(name = "busca", required = false) String busca,
            @RequestParam(name = "categoria", required = false) String categoria,
            @RequestParam(name = "ordenacao", defaultValue = "MAIS_RECENTES") OrdenacaoProduto ordenacao,
            @RequestParam(name = "ofertas", defaultValue = "false") boolean ofertas,
            @RequestParam(name = "pagina", defaultValue = "0") int pagina,
            @RequestParam(name = "tamanho", defaultValue = "12") int tamanho) {
        FiltroProdutos filtro = new FiltroProdutos(busca, categoria, ordenacao, ofertas);
        Pagina<Produto> resultado = buscarProdutos.buscar(filtro, new Paginacao(pagina, tamanho));
        Map<Long, ResumoAvaliacoes> avaliacoes = avaliacoesDe(resultado.itens());
        return PaginaResponse.de(resultado,
                produto -> ProdutoResumoResponse.de(produto, avaliacoes.get(produto.getId())));
    }

    @GetMapping("/destaques")
    public List<ProdutoResumoResponse> destaques(@RequestParam(name = "limite", defaultValue = "8") int limite) {
        return resumos(listarDestaques.listarDestaques(limite));
    }

    @GetMapping("/mais-vendidos")
    public List<ProdutoResumoResponse> maisVendidos(@RequestParam(name = "limite", defaultValue = "4") int limite) {
        return resumos(listarDestaques.listarMaisVendidos(limite));
    }

    @GetMapping("/{slug}")
    public ProdutoDetalheResponse detalhar(@PathVariable("slug") String slug) {
        Produto produto = detalharProduto.detalhar(slug);
        return ProdutoDetalheResponse.de(produto, avaliacoesDe(List.of(produto)).get(produto.getId()));
    }

    private List<ProdutoResumoResponse> resumos(List<Produto> produtos) {
        Map<Long, ResumoAvaliacoes> avaliacoes = avaliacoesDe(produtos);
        return produtos.stream()
                .map(produto -> ProdutoResumoResponse.de(produto, avaliacoes.get(produto.getId())))
                .toList();
    }

    private Map<Long, ResumoAvaliacoes> avaliacoesDe(List<Produto> produtos) {
        Map<Long, ResumoAvaliacoes> resumos = consultarAvaliacoes.resumosDosProdutos(
                produtos.stream().map(Produto::getId).toList());
        return resumos == null ? Map.of() : resumos;
    }
}
