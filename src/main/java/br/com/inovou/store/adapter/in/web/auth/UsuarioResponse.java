package br.com.inovou.store.adapter.in.web.auth;

import br.com.inovou.store.domain.usuario.Usuario;

public record UsuarioResponse(Long id, String nome, String email, String role) {

    public static UsuarioResponse de(Usuario usuario) {
        return new UsuarioResponse(usuario.getId(), usuario.getNome(), usuario.getEmail(), usuario.getRole().name());
    }
}
