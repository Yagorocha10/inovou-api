package br.com.inovou.store.adapter.in.web.admin;

import br.com.inovou.store.domain.usuario.Usuario;

public record ClienteResponse(Long id, String nome, String email) {

    static ClienteResponse de(Usuario usuario) {
        return usuario == null ? null : new ClienteResponse(usuario.getId(), usuario.getNome(), usuario.getEmail());
    }
}
