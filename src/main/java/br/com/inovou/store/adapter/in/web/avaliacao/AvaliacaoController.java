package br.com.inovou.store.adapter.in.web.avaliacao;

import br.com.inovou.store.application.autenticacao.UsuarioAutenticado;
import br.com.inovou.store.application.consulta.Paginacao;
import br.com.inovou.store.application.port.in.AvaliarProdutoUseCase;
import br.com.inovou.store.application.port.in.ConsultarAvaliacoesUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class AvaliacaoController {

    private final AvaliarProdutoUseCase avaliarProduto;
    private final ConsultarAvaliacoesUseCase consultarAvaliacoes;

    public AvaliacaoController(AvaliarProdutoUseCase avaliarProduto, ConsultarAvaliacoesUseCase consultarAvaliacoes) {
        this.avaliarProduto = avaliarProduto;
        this.consultarAvaliacoes = consultarAvaliacoes;
    }

    @GetMapping("/produtos/{slug}/avaliacoes")
    public AvaliacoesProdutoResponse listar(@PathVariable("slug") String slug,
                                            @RequestParam(name = "pagina", defaultValue = "0") int pagina,
                                            @RequestParam(name = "tamanho", defaultValue = "5") int tamanho) {
        return AvaliacoesProdutoResponse.de(consultarAvaliacoes.doProduto(slug, new Paginacao(pagina, tamanho)));
    }

    @GetMapping("/produtos/{slug}/avaliacoes/permissao")
    public PermissaoAvaliacaoResponse permissao(@AuthenticationPrincipal UsuarioAutenticado usuario,
                                                @PathVariable("slug") String slug) {
        return PermissaoAvaliacaoResponse.de(avaliarProduto.permissao(usuario == null ? null : usuario.id(), slug));
    }

    @PostMapping("/produtos/{slug}/avaliacoes")
    @ResponseStatus(HttpStatus.CREATED)
    public AvaliacaoResponse avaliar(@AuthenticationPrincipal UsuarioAutenticado usuario,
                                     @PathVariable("slug") String slug,
                                     @Valid @RequestBody AvaliacaoRequest request) {
        return AvaliacaoResponse.de(avaliarProduto.avaliar(usuario.id(), slug, request.nota(), request.comentario()));
    }

    @GetMapping("/avaliacoes/recentes")
    public List<DepoimentoResponse> recentes(@RequestParam(name = "limite", defaultValue = "3") int limite) {
        return consultarAvaliacoes.recentes(limite).stream().map(DepoimentoResponse::de).toList();
    }
}
