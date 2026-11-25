package br.com.inovou.store.application.port.in;

import br.com.inovou.store.application.autenticacao.Sessao;

public interface AutenticarUseCase {

    Sessao autenticar(String email, String senha);
}
