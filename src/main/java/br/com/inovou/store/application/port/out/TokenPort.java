package br.com.inovou.store.application.port.out;

import br.com.inovou.store.application.autenticacao.TokenEmitido;
import br.com.inovou.store.application.autenticacao.UsuarioAutenticado;
import br.com.inovou.store.domain.usuario.Usuario;

import java.util.Optional;

public interface TokenPort {

    TokenEmitido emitir(Usuario usuario);

    Optional<UsuarioAutenticado> validar(String token);
}
