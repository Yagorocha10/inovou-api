package br.com.inovou.store.adapter.in.web.produto;

import br.com.inovou.store.adapter.in.web.comum.PaginaResponse;
import br.com.inovou.store.application.consulta.FiltroProdutos;
import br.com.inovou.store.application.consulta.OrdenacaoProduto;
import br.com.inovou.store.application.consulta.Paginacao;
import br.com.inovou.store.application.port.in.BuscarProdutosUseCase;
import br.com.inovou.store.application.port.in.DetalharProdutoUseCase;
import br.com.inovou.store.application.port.in.ListarDestaquesUseCase;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/produtos")
public class ProdutoController {

    private final BuscarProdutosUseCase buscarProdutos;
    private final DetalharProdutoUseCase detalharProduto;
    private final ListarDestaquesUseCase listarDestaques;

    public ProdutoController(BuscarProdutosUseCase buscarProdutos,
                             DetalharProdutoUseCase detalharProduto,
                             ListarDestaquesUseCase listarDestaques) {
        this.buscarProdutos = buscarProdutos;
        this.detalharProduto = detalharProduto;
        this.listarDestaques = listarDestaques;
    }

    @GetMapping
    public PaginaResponse<ProdutoResumoResponse> buscar(
            @RequestParam(name = "busca", required = false) String busca,
            @RequestParam(name = "categoria", required = false) String categoria,
            @RequestParam(name = "ordenacao", defaultValue = "MAIS_RECENTES") OrdenacaoProduto ordenacao,
            @RequestParam(name = "pagina", defaultValue = "0") int pagina,
            @RequestParam(name = "tamanho", defaultValue = "12") int tamanho) {
        FiltroProdutos filtro = new FiltroProdutos(busca, categoria, ordenacao);
        return PaginaResponse.de(buscarProdutos.buscar(filtro, new Paginacao(pagina, tamanho)), ProdutoResumoResponse::de);
    }

    @GetMapping("/destaques")
    public List<ProdutoResumoResponse> destaques(@RequestParam(name = "limite", defaultValue = "8") int limite) {
        return listarDestaques.listarDestaques(limite).stream().map(ProdutoResumoResponse::de).toList();
    }

    @GetMapping("/{slug}")
    public ProdutoDetalheResponse detalhar(@PathVariable("slug") String slug) {
        return ProdutoDetalheResponse.de(detalharProduto.detalhar(slug));
    }
}
