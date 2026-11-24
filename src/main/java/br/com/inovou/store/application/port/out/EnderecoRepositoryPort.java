package br.com.inovou.store.application.port.out;

import br.com.inovou.store.domain.usuario.Endereco;

import java.util.List;
import java.util.Optional;

public interface EnderecoRepositoryPort {

    List<Endereco> listarDoUsuario(Long usuarioId);

    Optional<Endereco> buscarDoUsuario(Long enderecoId, Long usuarioId);

    Endereco salvar(Endereco endereco);

    void remover(Long enderecoId);
}
