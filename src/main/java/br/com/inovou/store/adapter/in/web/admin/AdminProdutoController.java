package br.com.inovou.store.adapter.in.web.admin;

import br.com.inovou.store.adapter.in.web.comum.PaginaResponse;
import br.com.inovou.store.application.consulta.ArquivoImagem;
import br.com.inovou.store.application.consulta.FiltroProdutosAdmin;
import br.com.inovou.store.application.consulta.Paginacao;
import br.com.inovou.store.application.consulta.SituacaoProduto;
import br.com.inovou.store.application.port.in.GerenciarImagensProdutoUseCase;
import br.com.inovou.store.application.port.in.GerenciarProdutosUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/admin/produtos")
public class AdminProdutoController {

    private static final int TAMANHO_PADRAO = 20;

    private final GerenciarProdutosUseCase produtos;
    private final GerenciarImagensProdutoUseCase imagens;

    public AdminProdutoController(GerenciarProdutosUseCase produtos, GerenciarImagensProdutoUseCase imagens) {
        this.produtos = produtos;
        this.imagens = imagens;
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

    @PostMapping(path = "/{id}/imagens", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ProdutoAdminResponse adicionarImagem(@PathVariable("id") Long id,
                                                @RequestParam("arquivo") MultipartFile arquivo) throws IOException {
        ArquivoImagem imagem = new ArquivoImagem(arquivo.getOriginalFilename(), arquivo.getContentType(),
                arquivo.getSize(), arquivo.getInputStream());
        return ProdutoAdminResponse.de(imagens.adicionar(id, imagem));
    }

    @DeleteMapping("/{id}/imagens/{imagemId}")
    public ProdutoAdminResponse removerImagem(@PathVariable("id") Long id, @PathVariable("imagemId") Long imagemId) {
        return ProdutoAdminResponse.de(imagens.remover(id, imagemId));
    }

    @PutMapping("/{id}/imagens/ordem")
    public ProdutoAdminResponse reordenarImagens(@PathVariable("id") Long id,
                                                 @Valid @RequestBody OrdemImagensRequest request) {
        return ProdutoAdminResponse.de(imagens.reordenar(id, request.ids()));
    }
}
