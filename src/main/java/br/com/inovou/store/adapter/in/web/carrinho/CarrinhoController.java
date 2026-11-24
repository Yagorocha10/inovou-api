package br.com.inovou.store.adapter.in.web.carrinho;

import br.com.inovou.store.application.autenticacao.UsuarioAutenticado;
import br.com.inovou.store.application.port.in.GerenciarCarrinhoUseCase;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/carrinho")
public class CarrinhoController {

    private final GerenciarCarrinhoUseCase carrinho;

    public CarrinhoController(GerenciarCarrinhoUseCase carrinho) {
        this.carrinho = carrinho;
    }

    @GetMapping
    public CarrinhoResponse buscar(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return CarrinhoResponse.de(carrinho.buscar(usuario.id()));
    }

    @PostMapping("/itens")
    public CarrinhoResponse adicionar(@AuthenticationPrincipal UsuarioAutenticado usuario,
                                      @Valid @RequestBody AdicionarItemRequest request) {
        return CarrinhoResponse.de(carrinho.adicionar(usuario.id(), request.produtoId(), request.quantidade()));
    }

    @PutMapping("/itens/{produtoId}")
    public CarrinhoResponse alterar(@AuthenticationPrincipal UsuarioAutenticado usuario,
                                    @PathVariable("produtoId") Long produtoId,
                                    @Valid @RequestBody AlterarQuantidadeRequest request) {
        return CarrinhoResponse.de(carrinho.alterarQuantidade(usuario.id(), produtoId, request.quantidade()));
    }

    @DeleteMapping("/itens/{produtoId}")
    public CarrinhoResponse remover(@AuthenticationPrincipal UsuarioAutenticado usuario,
                                    @PathVariable("produtoId") Long produtoId) {
        return CarrinhoResponse.de(carrinho.remover(usuario.id(), produtoId));
    }

    @DeleteMapping
    public CarrinhoResponse esvaziar(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return CarrinhoResponse.de(carrinho.esvaziar(usuario.id()));
    }
}
