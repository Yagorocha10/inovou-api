package br.com.inovou.store.application.port.in;

import br.com.inovou.store.application.autenticacao.DadosCadastro;
import br.com.inovou.store.application.autenticacao.Sessao;

public interface CadastrarClienteUseCase {

    Sessao cadastrar(DadosCadastro dados);
}
