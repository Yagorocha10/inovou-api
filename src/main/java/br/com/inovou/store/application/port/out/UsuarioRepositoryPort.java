package br.com.inovou.store.application.port.out;

import br.com.inovou.store.domain.usuario.Usuario;

import java.util.Optional;

public interface UsuarioRepositoryPort {

    Optional<Usuario> buscarPorEmail(String email);

    Optional<Usuario> buscarPorId(Long id);

    boolean existePorEmail(String email);

    Usuario salvar(Usuario usuario);
}
