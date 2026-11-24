package br.com.inovou.store.application.service;

import br.com.inovou.store.application.autenticacao.DadosCadastro;
import br.com.inovou.store.application.autenticacao.Sessao;
import br.com.inovou.store.application.autenticacao.TokenEmitido;
import br.com.inovou.store.application.port.out.SenhaEncoderPort;
import br.com.inovou.store.application.port.out.TokenPort;
import br.com.inovou.store.application.port.out.UsuarioRepositoryPort;
import br.com.inovou.store.domain.exception.CredenciaisInvalidasException;
import br.com.inovou.store.domain.exception.DomainException;
import br.com.inovou.store.domain.exception.EmailJaCadastradoException;
import br.com.inovou.store.domain.usuario.Role;
import br.com.inovou.store.domain.usuario.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AutenticacaoServiceTest {

    @Mock
    private UsuarioRepositoryPort usuarioRepository;

    @Mock
    private SenhaEncoderPort senhaEncoder;

    @Mock
    private TokenPort tokenPort;

    private AutenticacaoService service;

    private final TokenEmitido token = new TokenEmitido("jwt", Instant.parse("2026-11-24T20:00:00Z"));
    private final Usuario yago = new Usuario(7L, "Yago", "yago@exemplo.com", "hash", Role.CLIENTE);

    @BeforeEach
    void setUp() {
        service = new AutenticacaoService(usuarioRepository, senhaEncoder, tokenPort);
    }

    @Test
    void deveCadastrarClienteComSenhaCodificadaEDevolverToken() {
        when(usuarioRepository.existePorEmail("yago@exemplo.com")).thenReturn(false);
        when(senhaEncoder.codificar("senhaForte1")).thenReturn("hash");
        when(usuarioRepository.salvar(any(Usuario.class))).thenReturn(yago);
        when(tokenPort.emitir(yago)).thenReturn(token);

        Sessao sessao = service.cadastrar(new DadosCadastro("Yago", " Yago@Exemplo.com ", "senhaForte1"));

        ArgumentCaptor<Usuario> salvo = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).salvar(salvo.capture());
        assertThat(salvo.getValue().getEmail()).isEqualTo("yago@exemplo.com");
        assertThat(salvo.getValue().getSenhaHash()).isEqualTo("hash");
        assertThat(salvo.getValue().getRole()).isEqualTo(Role.CLIENTE);
        assertThat(sessao.token()).isEqualTo(token);
    }

    @Test
    void naoDeveCadastrarEmailRepetido() {
        when(usuarioRepository.existePorEmail("yago@exemplo.com")).thenReturn(true);

        assertThatThrownBy(() -> service.cadastrar(new DadosCadastro("Yago", "yago@exemplo.com", "senhaForte1")))
                .isInstanceOf(EmailJaCadastradoException.class);
        verify(usuarioRepository, never()).salvar(any());
    }

    @Test
    void deveRecusarSenhaCurta() {
        assertThatThrownBy(() -> service.cadastrar(new DadosCadastro("Yago", "yago@exemplo.com", "1234567")))
                .isInstanceOf(DomainException.class)
                .hasMessage("A senha deve ter pelo menos 8 caracteres");
    }

    @Test
    void deveAutenticarComSenhaCorreta() {
        when(usuarioRepository.buscarPorEmail("yago@exemplo.com")).thenReturn(Optional.of(yago));
        when(senhaEncoder.confere("senhaForte1", "hash")).thenReturn(true);
        when(tokenPort.emitir(yago)).thenReturn(token);

        Sessao sessao = service.autenticar("YAGO@exemplo.com", "senhaForte1");

        assertThat(sessao.usuario()).isSameAs(yago);
    }

    @Test
    void deveRecusarSenhaErradaComAMesmaMensagemDeEmailInexistente() {
        when(usuarioRepository.buscarPorEmail("yago@exemplo.com")).thenReturn(Optional.of(yago));
        when(senhaEncoder.confere("errada", "hash")).thenReturn(false);
        when(usuarioRepository.buscarPorEmail("ninguem@exemplo.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.autenticar("yago@exemplo.com", "errada"))
                .isInstanceOf(CredenciaisInvalidasException.class)
                .hasMessage("E-mail ou senha inválidos");
        assertThatThrownBy(() -> service.autenticar("ninguem@exemplo.com", "qualquer"))
                .isInstanceOf(CredenciaisInvalidasException.class)
                .hasMessage("E-mail ou senha inválidos");
        verify(tokenPort, never()).emitir(any());
    }

    @Test
    void tokenVazioNaoEhValido() {
        assertThat(service.validar(" ")).isEmpty();
        verify(tokenPort, never()).validar(any());
    }
}
