package br.com.inovou.store.adapter.out.persistence.usuario;

import br.com.inovou.store.application.port.out.EnderecoRepositoryPort;
import br.com.inovou.store.domain.usuario.Endereco;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
@Transactional(readOnly = true)
public class EnderecoPersistenceAdapter implements EnderecoRepositoryPort {

    private final EnderecoJpaRepository enderecoJpaRepository;

    public EnderecoPersistenceAdapter(EnderecoJpaRepository enderecoJpaRepository) {
        this.enderecoJpaRepository = enderecoJpaRepository;
    }

    @Override
    public List<Endereco> listarDoUsuario(Long usuarioId) {
        return enderecoJpaRepository.findByUsuarioIdOrderByIdAsc(usuarioId).stream()
                .map(EnderecoPersistenceAdapter::paraDominio)
                .toList();
    }

    @Override
    public Optional<Endereco> buscarDoUsuario(Long enderecoId, Long usuarioId) {
        return enderecoJpaRepository.findByIdAndUsuarioId(enderecoId, usuarioId)
                .map(EnderecoPersistenceAdapter::paraDominio);
    }

    @Override
    @Transactional
    public Endereco salvar(Endereco endereco) {
        EnderecoJpaEntity entidade = endereco.id() == null
                ? new EnderecoJpaEntity()
                : enderecoJpaRepository.findById(endereco.id()).orElseGet(EnderecoJpaEntity::new);
        entidade.setUsuarioId(endereco.usuarioId());
        entidade.setCep(endereco.cep());
        entidade.setLogradouro(endereco.logradouro());
        entidade.setNumero(endereco.numero());
        entidade.setComplemento(endereco.complemento());
        entidade.setBairro(endereco.bairro());
        entidade.setCidade(endereco.cidade());
        entidade.setUf(endereco.uf());
        return paraDominio(enderecoJpaRepository.save(entidade));
    }

    @Override
    @Transactional
    public void remover(Long enderecoId) {
        enderecoJpaRepository.deleteById(enderecoId);
    }

    private static Endereco paraDominio(EnderecoJpaEntity entidade) {
        return new Endereco(entidade.getId(), entidade.getUsuarioId(), entidade.getCep(), entidade.getLogradouro(),
                entidade.getNumero(), entidade.getComplemento(), entidade.getBairro(), entidade.getCidade(),
                entidade.getUf());
    }
}
