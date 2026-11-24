package br.com.inovou.store.application.autenticacao;

import br.com.inovou.store.domain.usuario.Role;

public record UsuarioAutenticado(Long id, String nome, String email, Role role) {
}
