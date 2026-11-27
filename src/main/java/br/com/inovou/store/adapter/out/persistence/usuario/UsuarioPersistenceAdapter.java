package br.com.inovou.store.adapter.out.persistence.usuario;

import br.com.inovou.store.application.port.out.UsuarioRepositoryPort;
import br.com.inovou.store.domain.usuario.Usuario;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component
@Transactional(readOnly = true)
public class UsuarioPersistenceAdapter implements UsuarioRepositoryPort {

    private final UsuarioJpaRepository usuarioJpaRepository;

    public UsuarioPersistenceAdapter(UsuarioJpaRepository usuarioJpaRepository) {
        this.usuarioJpaRepository = usuarioJpaRepository;
    }

    @Override
    public Optional<Usuario> buscarPorEmail(String email) {
        return usuarioJpaRepository.findByEmail(email).map(UsuarioPersistenceAdapter::paraDominio);
    }

    @Override
    public Optional<Usuario> buscarPorId(Long id) {
        return usuarioJpaRepository.findById(id).map(UsuarioPersistenceAdapter::paraDominio);
    }

    @Override
    public boolean existePorEmail(String email) {
        return usuarioJpaRepository.existsByEmail(email);
    }

    @Override
    @Transactional
    public Usuario salvar(Usuario usuario) {
        UsuarioJpaEntity entidade = usuario.getId() == null
                ? new UsuarioJpaEntity()
                : usuarioJpaRepository.findById(usuario.getId()).orElseGet(UsuarioJpaEntity::new);
        entidade.setNome(usuario.getNome());
        entidade.setEmail(usuario.getEmail());
        entidade.setSenhaHash(usuario.getSenhaHash());
        entidade.setRole(usuario.getRole());
        return paraDominio(usuarioJpaRepository.save(entidade));
    }

    @Override
    public List<Usuario> buscarPorIds(Set<Long> ids) {
        return usuarioJpaRepository.findAllById(ids).stream().map(UsuarioPersistenceAdapter::paraDominio).toList();
    }

    private static Usuario paraDominio(UsuarioJpaEntity entidade) {
        return new Usuario(entidade.getId(), entidade.getNome(), entidade.getEmail(),
                entidade.getSenhaHash(), entidade.getRole());
    }
}
