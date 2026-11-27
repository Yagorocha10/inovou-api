package br.com.inovou.store.adapter.in.web.admin;

import br.com.inovou.store.application.port.in.ConsultarResumoAdminUseCase;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminPainelController {

    private final ConsultarResumoAdminUseCase painel;

    public AdminPainelController(ConsultarResumoAdminUseCase painel) {
        this.painel = painel;
    }

    @GetMapping("/resumo")
    public ResumoAdminResponse resumo() {
        return ResumoAdminResponse.de(painel.resumo());
    }
}
