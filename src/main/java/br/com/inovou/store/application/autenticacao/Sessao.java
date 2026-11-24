package br.com.inovou.store.application.autenticacao;

import br.com.inovou.store.domain.usuario.Usuario;

public record Sessao(TokenEmitido token, Usuario usuario) {
}
