package br.com.inovou.store.application.port.in;

import br.com.inovou.store.application.autenticacao.DadosEndereco;
import br.com.inovou.store.domain.usuario.Endereco;

import java.util.List;

public interface GerenciarEnderecosUseCase {

    List<Endereco> listar(Long usuarioId);

    Endereco criar(Long usuarioId, DadosEndereco dados);

    Endereco atualizar(Long usuarioId, Long enderecoId, DadosEndereco dados);

    void remover(Long usuarioId, Long enderecoId);
}
