package br.com.inovou.store.adapter.in.web.conta;

import br.com.inovou.store.adapter.in.web.auth.UsuarioResponse;
import br.com.inovou.store.application.autenticacao.UsuarioAutenticado;
import br.com.inovou.store.application.port.in.ConsultarUsuarioUseCase;
import br.com.inovou.store.application.port.in.GerenciarEnderecosUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/me")
public class ContaController {

    private final ConsultarUsuarioUseCase consultarUsuario;
    private final GerenciarEnderecosUseCase gerenciarEnderecos;

    public ContaController(ConsultarUsuarioUseCase consultarUsuario, GerenciarEnderecosUseCase gerenciarEnderecos) {
        this.consultarUsuario = consultarUsuario;
        this.gerenciarEnderecos = gerenciarEnderecos;
    }

    @GetMapping
    public UsuarioResponse eu(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return UsuarioResponse.de(consultarUsuario.buscar(usuario.id()));
    }

    @GetMapping("/enderecos")
    public List<EnderecoResponse> listarEnderecos(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return gerenciarEnderecos.listar(usuario.id()).stream().map(EnderecoResponse::de).toList();
    }

    @PostMapping("/enderecos")
    @ResponseStatus(HttpStatus.CREATED)
    public EnderecoResponse criarEndereco(@AuthenticationPrincipal UsuarioAutenticado usuario,
                                          @Valid @RequestBody EnderecoRequest request) {
        return EnderecoResponse.de(gerenciarEnderecos.criar(usuario.id(), request.paraDados()));
    }

    @PutMapping("/enderecos/{id}")
    public EnderecoResponse atualizarEndereco(@AuthenticationPrincipal UsuarioAutenticado usuario,
                                              @PathVariable("id") Long id,
                                              @Valid @RequestBody EnderecoRequest request) {
        return EnderecoResponse.de(gerenciarEnderecos.atualizar(usuario.id(), id, request.paraDados()));
    }

    @DeleteMapping("/enderecos/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removerEndereco(@AuthenticationPrincipal UsuarioAutenticado usuario, @PathVariable("id") Long id) {
        gerenciarEnderecos.remover(usuario.id(), id);
    }
}
