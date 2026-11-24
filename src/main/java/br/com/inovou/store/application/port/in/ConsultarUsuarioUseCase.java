package br.com.inovou.store.application.port.in;

import br.com.inovou.store.domain.usuario.Usuario;

public interface ConsultarUsuarioUseCase {

    Usuario buscar(Long id);
}
