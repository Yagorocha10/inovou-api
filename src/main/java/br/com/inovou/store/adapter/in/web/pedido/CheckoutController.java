package br.com.inovou.store.adapter.in.web.pedido;

import br.com.inovou.store.application.autenticacao.UsuarioAutenticado;
import br.com.inovou.store.application.port.in.ResumirCheckoutUseCase;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/checkout")
public class CheckoutController {

    private final ResumirCheckoutUseCase checkout;

    public CheckoutController(ResumirCheckoutUseCase checkout) {
        this.checkout = checkout;
    }

    @GetMapping("/resumo")
    public ResumoCheckoutResponse resumo(@AuthenticationPrincipal UsuarioAutenticado usuario,
                                         @RequestParam("enderecoId") Long enderecoId) {
        return ResumoCheckoutResponse.de(checkout.resumir(usuario.id(), enderecoId));
    }
}
