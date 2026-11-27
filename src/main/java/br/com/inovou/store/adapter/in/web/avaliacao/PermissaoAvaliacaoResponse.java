package br.com.inovou.store.adapter.in.web.avaliacao;

import br.com.inovou.store.application.consulta.PermissaoAvaliacao;

public record PermissaoAvaliacaoResponse(boolean podeAvaliar, String motivo) {

    static PermissaoAvaliacaoResponse de(PermissaoAvaliacao permissao) {
        return new PermissaoAvaliacaoResponse(permissao.podeAvaliar(), permissao.motivo());
    }
}
