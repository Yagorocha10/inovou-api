package br.com.inovou.store.application.service;

import br.com.inovou.store.application.autenticacao.DadosEndereco;
import br.com.inovou.store.application.port.in.GerenciarEnderecosUseCase;
import br.com.inovou.store.application.port.out.EnderecoRepositoryPort;
import br.com.inovou.store.domain.exception.DomainException;
import br.com.inovou.store.domain.exception.RecursoNaoEncontradoException;
import br.com.inovou.store.domain.usuario.Endereco;

import java.util.List;

public class EnderecoService implements GerenciarEnderecosUseCase {

    static final int LIMITE_ENDERECOS = 5;

    private final EnderecoRepositoryPort enderecoRepository;

    public EnderecoService(EnderecoRepositoryPort enderecoRepository) {
        this.enderecoRepository = enderecoRepository;
    }

    @Override
    public List<Endereco> listar(Long usuarioId) {
        return enderecoRepository.listarDoUsuario(usuarioId);
    }

    @Override
    public Endereco criar(Long usuarioId, DadosEndereco dados) {
        if (enderecoRepository.listarDoUsuario(usuarioId).size() >= LIMITE_ENDERECOS) {
            throw new DomainException("Você pode cadastrar no máximo %d endereços".formatted(LIMITE_ENDERECOS));
        }
        return enderecoRepository.salvar(montar(null, usuarioId, dados));
    }

    @Override
    public Endereco atualizar(Long usuarioId, Long enderecoId, DadosEndereco dados) {
        buscarDoUsuario(usuarioId, enderecoId);
        return enderecoRepository.salvar(montar(enderecoId, usuarioId, dados));
    }

    @Override
    public void remover(Long usuarioId, Long enderecoId) {
        buscarDoUsuario(usuarioId, enderecoId);
        enderecoRepository.remover(enderecoId);
    }

    private Endereco buscarDoUsuario(Long usuarioId, Long enderecoId) {
        return enderecoRepository.buscarDoUsuario(enderecoId, usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Endereço não encontrado"));
    }

    private static Endereco montar(Long id, Long usuarioId, DadosEndereco dados) {
        return new Endereco(id, usuarioId, dados.cep(), dados.logradouro(), dados.numero(),
                dados.complemento(), dados.bairro(), dados.cidade(), dados.uf());
    }
}
