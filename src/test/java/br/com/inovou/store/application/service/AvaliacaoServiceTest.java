package br.com.inovou.store.application.service;

import br.com.inovou.store.application.consulta.PermissaoAvaliacao;
import br.com.inovou.store.application.port.out.AvaliacaoRepositoryPort;
import br.com.inovou.store.application.port.out.PedidoRepositoryPort;
import br.com.inovou.store.application.port.out.ProdutoRepositoryPort;
import br.com.inovou.store.application.port.out.UsuarioRepositoryPort;
import br.com.inovou.store.domain.avaliacao.Avaliacao;
import br.com.inovou.store.domain.exception.DomainException;
import br.com.inovou.store.domain.exception.RecursoNaoEncontradoException;
import br.com.inovou.store.domain.produto.Categoria;
import br.com.inovou.store.domain.produto.Produto;
import br.com.inovou.store.domain.usuario.Role;
import br.com.inovou.store.domain.usuario.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AvaliacaoServiceTest {

    private static final Long CLIENTE = 7L;
    private static final String SLUG = "console-portatil-r36";

    @Mock
    private AvaliacaoRepositoryPort avaliacaoRepository;

    @Mock
    private ProdutoRepositoryPort produtoRepository;

    @Mock
    private PedidoRepositoryPort pedidoRepository;

    @Mock
    private UsuarioRepositoryPort usuarioRepository;

    private AvaliacaoService service;

    private final Produto console = new Produto(4L, "Console portátil R36", SLUG, "", new BigDecimal("40.00"), 5,
            true, new Categoria(2L, "Games", "games"), List.of());

    @BeforeEach
    void setUp() {
        Clock relogio = Clock.fixed(Instant.parse("2026-11-27T13:00:00Z"), ZoneId.of("America/Belem"));
        service = new AvaliacaoService(avaliacaoRepository, produtoRepository, pedidoRepository, usuarioRepository,
                relogio);
    }

    private void consoleExiste() {
        when(produtoRepository.buscarAtivoPorSlug(SLUG)).thenReturn(Optional.of(console));
    }

    @Test
    void clienteQueRecebeuOProdutoAvaliaComONomeResumido() {
        consoleExiste();
        when(avaliacaoRepository.existe(4L, CLIENTE)).thenReturn(false);
        when(pedidoRepository.clienteRecebeuProduto(CLIENTE, 4L)).thenReturn(true);
        when(usuarioRepository.buscarPorId(CLIENTE))
                .thenReturn(Optional.of(new Usuario(CLIENTE, "Yago da Silva Rocha", "yago@teste.com", "hash", Role.CLIENTE)));
        when(avaliacaoRepository.salvar(any(Avaliacao.class))).thenAnswer(chamada -> chamada.getArgument(0));

        service.avaliar(CLIENTE, SLUG, 5, "Chegou rápido e funciona muito bem");

        ArgumentCaptor<Avaliacao> salva = ArgumentCaptor.forClass(Avaliacao.class);
        verify(avaliacaoRepository).salvar(salva.capture());
        assertThat(salva.getValue().produtoId()).isEqualTo(4L);
        assertThat(salva.getValue().autor()).isEqualTo("Yago R.");
        assertThat(salva.getValue().nota()).isEqualTo(5);
        assertThat(salva.getValue().criadoEm()).isEqualTo(LocalDateTime.of(2026, 11, 27, 10, 0));
    }

    @Test
    void quemAindaNaoRecebeuOProdutoNaoAvalia() {
        consoleExiste();
        when(avaliacaoRepository.existe(4L, CLIENTE)).thenReturn(false);
        when(pedidoRepository.clienteRecebeuProduto(CLIENTE, 4L)).thenReturn(false);

        assertThatThrownBy(() -> service.avaliar(CLIENTE, SLUG, 5, null))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("depois de recebê-lo");
        verify(avaliacaoRepository, never()).salvar(any());
    }

    @Test
    void cadaClienteAvaliaOProdutoUmaVezSo() {
        consoleExiste();
        when(avaliacaoRepository.existe(4L, CLIENTE)).thenReturn(true);

        PermissaoAvaliacao permissao = service.permissao(CLIENTE, SLUG);

        assertThat(permissao.podeAvaliar()).isFalse();
        assertThat(permissao.motivo()).contains("já avaliou");
    }

    @Test
    void visitantePrecisaEntrarNaConta() {
        consoleExiste();

        PermissaoAvaliacao permissao = service.permissao(null, SLUG);

        assertThat(permissao.podeAvaliar()).isFalse();
        assertThat(permissao.motivo()).contains("Entre na sua conta");
        verifyNoInteractions(avaliacaoRepository, pedidoRepository);
    }

    @Test
    void produtoInexistenteDa404() {
        when(produtoRepository.buscarAtivoPorSlug("nao-existe")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.permissao(CLIENTE, "nao-existe"))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    void resumosDeNenhumProdutoNaoConsultamOBanco() {
        assertThat(service.resumosDosProdutos(List.of())).isEqualTo(Map.of());
        verifyNoInteractions(avaliacaoRepository);
    }

    @Test
    void limitaOsDepoimentosRecentes() {
        service.recentes(500);
        service.recentes(0);

        verify(avaliacaoRepository).recentesComComentario(AvaliacaoService.LIMITE_MAXIMO_RECENTES);
        verify(avaliacaoRepository).recentesComComentario(1);
    }
}
