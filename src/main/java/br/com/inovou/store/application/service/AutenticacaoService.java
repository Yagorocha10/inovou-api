package br.com.inovou.store.application.service;

import br.com.inovou.store.application.autenticacao.DadosCadastro;
import br.com.inovou.store.application.autenticacao.Sessao;
import br.com.inovou.store.application.autenticacao.UsuarioAutenticado;
import br.com.inovou.store.application.port.in.AutenticarUseCase;
import br.com.inovou.store.application.port.in.CadastrarClienteUseCase;
import br.com.inovou.store.application.port.in.ValidarTokenUseCase;
import br.com.inovou.store.application.port.out.SenhaEncoderPort;
import br.com.inovou.store.application.port.out.TokenPort;
import br.com.inovou.store.application.port.out.UsuarioRepositoryPort;
import br.com.inovou.store.domain.exception.CredenciaisInvalidasException;
import br.com.inovou.store.domain.exception.DomainException;
import br.com.inovou.store.domain.exception.EmailJaCadastradoException;
import br.com.inovou.store.domain.usuario.Usuario;

import java.util.Optional;

public class AutenticacaoService implements CadastrarClienteUseCase, AutenticarUseCase, ValidarTokenUseCase {

    static final int TAMANHO_MINIMO_SENHA = 8;
    static final int TAMANHO_MAXIMO_SENHA = 72;

    private final UsuarioRepositoryPort usuarioRepository;
    private final SenhaEncoderPort senhaEncoder;
    private final TokenPort tokenPort;

    public AutenticacaoService(UsuarioRepositoryPort usuarioRepository, SenhaEncoderPort senhaEncoder,
                               TokenPort tokenPort) {
        this.usuarioRepository = usuarioRepository;
        this.senhaEncoder = senhaEncoder;
        this.tokenPort = tokenPort;
    }

    @Override
    public Sessao cadastrar(DadosCadastro dados) {
        validarSenha(dados.senha());
        String email = Usuario.normalizarEmail(dados.email());
        if (usuarioRepository.existePorEmail(email)) {
            throw new EmailJaCadastradoException(email);
        }
        Usuario novo = Usuario.novoCliente(dados.nome(), email, senhaEncoder.codificar(dados.senha()));
        Usuario salvo = usuarioRepository.salvar(novo);
        return new Sessao(tokenPort.emitir(salvo), salvo);
    }

    @Override
    public Sessao autenticar(String email, String senha) {
        if (email == null || email.isBlank() || senha == null || senha.isEmpty()) {
            throw new CredenciaisInvalidasException();
        }
        Usuario usuario = usuarioRepository.buscarPorEmail(email.strip().toLowerCase())
                .filter(encontrado -> senhaEncoder.confere(senha, encontrado.getSenhaHash()))
                .orElseThrow(CredenciaisInvalidasException::new);
        return new Sessao(tokenPort.emitir(usuario), usuario);
    }

    @Override
    public Optional<UsuarioAutenticado> validar(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        return tokenPort.validar(token);
    }

    private static void validarSenha(String senha) {
        if (senha == null || senha.length() < TAMANHO_MINIMO_SENHA) {
            throw new DomainException("A senha deve ter pelo menos %d caracteres".formatted(TAMANHO_MINIMO_SENHA));
        }
        if (senha.length() > TAMANHO_MAXIMO_SENHA) {
            throw new DomainException("A senha pode ter no máximo %d caracteres".formatted(TAMANHO_MAXIMO_SENHA));
        }
    }
}
