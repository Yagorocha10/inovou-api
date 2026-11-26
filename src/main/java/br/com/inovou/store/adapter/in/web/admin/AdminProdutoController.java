package br.com.inovou.store.adapter.in.web.admin;

import br.com.inovou.store.adapter.in.web.comum.PaginaResponse;
import br.com.inovou.store.application.consulta.FiltroProdutosAdmin;
import br.com.inovou.store.application.consulta.Paginacao;
import br.com.inovou.store.application.consulta.SituacaoProduto;
import br.com.inovou.store.application.port.in.GerenciarProdutosUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/produtos")
public class AdminProdutoController {

    private static final int TAMANHO_PADRAO = 20;

    private final GerenciarProdutosUseCase produtos;

    public AdminProdutoController(GerenciarProdutosUseCase produtos) {
        this.produtos = produtos;
    }

    @GetMapping
    public PaginaResponse<ProdutoAdminResponse> listar(
            @RequestParam(name = "busca", required = false) String busca,
            @RequestParam(name = "categoria", required = false) String categoria,
            @RequestParam(name = "situacao", required = false) SituacaoProduto situacao,
            @RequestParam(name = "pagina", required = false) Integer pagina,
            @RequestParam(name = "tamanho", required = false) Integer tamanho) {
        Paginacao paginacao = Paginacao.de(pagina, tamanho == null ? TAMANHO_PADRAO : tamanho);
        return PaginaResponse.de(produtos.listar(new FiltroProdutosAdmin(busca, categoria, situacao), paginacao),
                ProdutoAdminResponse::de);
    }

    @GetMapping("/{id}")
    public ProdutoAdminResponse buscar(@PathVariable("id") Long id) {
        return ProdutoAdminResponse.de(produtos.buscar(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProdutoAdminResponse criar(@Valid @RequestBody ProdutoRequest request) {
        return ProdutoAdminResponse.de(produtos.criar(request.paraDados()));
    }

    @PutMapping("/{id}")
    public ProdutoAdminResponse atualizar(@PathVariable("id") Long id, @Valid @RequestBody ProdutoRequest request) {
        return ProdutoAdminResponse.de(produtos.atualizar(id, request.paraDados()));
    }

    @PatchMapping("/{id}/ativo")
    public ProdutoAdminResponse alterarAtivo(@PathVariable("id") Long id, @Valid @RequestBody AtivoRequest request) {
        return ProdutoAdminResponse.de(produtos.alterarAtivo(id, request.ativo()));
    }
}
