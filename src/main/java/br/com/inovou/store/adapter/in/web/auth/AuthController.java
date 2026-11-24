package br.com.inovou.store.adapter.in.web.auth;

import br.com.inovou.store.application.autenticacao.DadosCadastro;
import br.com.inovou.store.application.port.in.AutenticarUseCase;
import br.com.inovou.store.application.port.in.CadastrarClienteUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final CadastrarClienteUseCase cadastrarCliente;
    private final AutenticarUseCase autenticar;

    public AuthController(CadastrarClienteUseCase cadastrarCliente, AutenticarUseCase autenticar) {
        this.cadastrarCliente = cadastrarCliente;
        this.autenticar = autenticar;
    }

    @PostMapping("/cadastro")
    @ResponseStatus(HttpStatus.CREATED)
    public SessaoResponse cadastrar(@Valid @RequestBody CadastroRequest request) {
        DadosCadastro dados = new DadosCadastro(request.nome(), request.email(), request.senha());
        return SessaoResponse.de(cadastrarCliente.cadastrar(dados));
    }

    @PostMapping("/login")
    public SessaoResponse login(@Valid @RequestBody LoginRequest request) {
        return SessaoResponse.de(autenticar.autenticar(request.email(), request.senha()));
    }
}
