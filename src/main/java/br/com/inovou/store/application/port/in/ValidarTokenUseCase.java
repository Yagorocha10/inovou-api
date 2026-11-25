package br.com.inovou.store.application.port.in;

import br.com.inovou.store.application.autenticacao.UsuarioAutenticado;

import java.util.Optional;

public interface ValidarTokenUseCase {

    Optional<UsuarioAutenticado> validar(String token);
}
