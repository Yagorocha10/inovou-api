package br.com.inovou.store.adapter.in.web.auth;

import br.com.inovou.store.application.autenticacao.Sessao;

public record SessaoResponse(String token, String expiraEm, UsuarioResponse usuario) {

    public static SessaoResponse de(Sessao sessao) {
        return new SessaoResponse(sessao.token().valor(), sessao.token().expiraEm().toString(),
                UsuarioResponse.de(sessao.usuario()));
    }
}
