package br.com.inovou.store.application.service;

import br.com.inovou.store.application.autenticacao.DadosEndereco;
import br.com.inovou.store.application.port.out.EnderecoRepositoryPort;
import br.com.inovou.store.domain.exception.DomainException;
import br.com.inovou.store.domain.exception.RecursoNaoEncontradoException;
import br.com.inovou.store.domain.usuario.Endereco;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EnderecoServiceTest {

    @Mock
    private EnderecoRepositoryPort enderecoRepository;

    private EnderecoService service;

    private final DadosEndereco dados = new DadosEndereco("66035170", "Av. Nazaré", "100", null, "Nazaré", "Belém", "PA");

    @BeforeEach
    void setUp() {
        service = new EnderecoService(enderecoRepository);
    }

    @Test
    void deveCriarEnderecoDoUsuario() {
        when(enderecoRepository.listarDoUsuario(1L)).thenReturn(List.of());
        when(enderecoRepository.salvar(any(Endereco.class))).thenAnswer(chamada -> {
            Endereco recebido = chamada.getArgument(0);
            return recebido.comId(10L);
        });

        Endereco criado = service.criar(1L, dados);

        assertThat(criado.id()).isEqualTo(10L);
        assertThat(criado.usuarioId()).isEqualTo(1L);
        assertThat(criado.cep()).isEqualTo("66035-170");
    }

    @Test
    void deveLimitarQuantidadeDeEnderecos() {
        Endereco existente = new Endereco(1L, 1L, "66035170", "Rua", "1", null, "Bairro", "Belém", "PA");
        when(enderecoRepository.listarDoUsuario(1L))
                .thenReturn(Collections.nCopies(EnderecoService.LIMITE_ENDERECOS, existente));

        assertThatThrownBy(() -> service.criar(1L, dados)).isInstanceOf(DomainException.class);
        verify(enderecoRepository, never()).salvar(any());
    }

    @Test
    void naoDeveAlterarEnderecoDeOutroUsuario() {
        when(enderecoRepository.buscarDoUsuario(10L, 2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.atualizar(2L, 10L, dados))
                .isInstanceOf(RecursoNaoEncontradoException.class);
        assertThatThrownBy(() -> service.remover(2L, 10L))
                .isInstanceOf(RecursoNaoEncontradoException.class);
        verify(enderecoRepository, never()).salvar(any());
        verify(enderecoRepository, never()).remover(any());
    }
}
