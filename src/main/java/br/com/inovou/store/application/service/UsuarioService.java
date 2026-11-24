package br.com.inovou.store.application.service;

import br.com.inovou.store.application.port.in.ConsultarUsuarioUseCase;
import br.com.inovou.store.application.port.out.UsuarioRepositoryPort;
import br.com.inovou.store.domain.exception.RecursoNaoEncontradoException;
import br.com.inovou.store.domain.usuario.Usuario;

public class UsuarioService implements ConsultarUsuarioUseCase {

    private final UsuarioRepositoryPort usuarioRepository;

    public UsuarioService(UsuarioRepositoryPort usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public Usuario buscar(Long id) {
        return usuarioRepository.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado"));
    }
}
