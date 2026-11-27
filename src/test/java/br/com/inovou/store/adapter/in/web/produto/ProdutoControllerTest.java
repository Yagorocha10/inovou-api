package br.com.inovou.store.adapter.in.web.produto;

import br.com.inovou.store.adapter.in.web.comum.GlobalExceptionHandler;
import br.com.inovou.store.application.consulta.FiltroProdutos;
import br.com.inovou.store.application.consulta.OrdenacaoProduto;
import br.com.inovou.store.application.consulta.Pagina;
import br.com.inovou.store.application.consulta.Paginacao;
import br.com.inovou.store.application.consulta.ResumoAvaliacoes;
import br.com.inovou.store.application.port.in.BuscarProdutosUseCase;
import br.com.inovou.store.application.port.in.ConsultarAvaliacoesUseCase;
import br.com.inovou.store.application.port.in.DetalharProdutoUseCase;
import br.com.inovou.store.application.port.in.ListarDestaquesUseCase;
import br.com.inovou.store.domain.exception.RecursoNaoEncontradoException;
import br.com.inovou.store.domain.produto.Categoria;
import br.com.inovou.store.domain.produto.ImagemProduto;
import br.com.inovou.store.domain.produto.Produto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ProdutoControllerTest {

    @Mock
    private BuscarProdutosUseCase buscarProdutos;

    @Mock
    private DetalharProdutoUseCase detalharProduto;

    @Mock
    private ListarDestaquesUseCase listarDestaques;

    @Mock
    private ConsultarAvaliacoesUseCase consultarAvaliacoes;

    private MockMvc mockMvc;

    private final Produto controle = new Produto(1L, "Controle PS4 sem fio Bluetooth", null, "Vibração dupla",
            new BigDecimal("129.90"), 3, true, new Categoria(3L, "Games e controles", null),
            List.of(new ImagemProduto(10L, "/uploads/controle.jpg", 0)));

    @BeforeEach
    void setUp() {
        ProdutoController controller = new ProdutoController(buscarProdutos, detalharProduto, listarDestaques,
                consultarAvaliacoes);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void deveListarProdutosPaginados() throws Exception {
        FiltroProdutos filtro = new FiltroProdutos("controle", "games-e-controles", OrdenacaoProduto.MENOR_PRECO);
        when(buscarProdutos.buscar(filtro, new Paginacao(0, 12)))
                .thenReturn(new Pagina<>(List.of(controle), 0, 12, 1));

        mockMvc.perform(get("/api/produtos")
                        .param("busca", "controle")
                        .param("categoria", "games-e-controles")
                        .param("ordenacao", "MENOR_PRECO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalItens").value(1))
                .andExpect(jsonPath("$.totalPaginas").value(1))
                .andExpect(jsonPath("$.itens[0].slug").value("controle-ps4-sem-fio-bluetooth"))
                .andExpect(jsonPath("$.itens[0].preco").value(129.9))
                .andExpect(jsonPath("$.itens[0].imagemPrincipal").value("/uploads/controle.jpg"))
                .andExpect(jsonPath("$.itens[0].categoria.slug").value("games-e-controles"))
                .andExpect(jsonPath("$.itens[0].disponivel").value(true));
    }

    @Test
    void deveDetalharProdutoPeloSlug() throws Exception {
        when(detalharProduto.detalhar("controle-ps4-sem-fio-bluetooth")).thenReturn(controle);

        mockMvc.perform(get("/api/produtos/controle-ps4-sem-fio-bluetooth"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Controle PS4 sem fio Bluetooth"))
                .andExpect(jsonPath("$.estoque").value(3))
                .andExpect(jsonPath("$.imagens[0]").value("/uploads/controle.jpg"));
    }

    @Test
    void deveResponder404QuandoProdutoNaoExiste() throws Exception {
        when(detalharProduto.detalhar("nao-existe"))
                .thenThrow(new RecursoNaoEncontradoException("Produto não encontrado: nao-existe"));

        mockMvc.perform(get("/api/produtos/nao-existe"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.mensagem").value("Produto não encontrado: nao-existe"));
    }

    @Test
    void deveResponder400ParaOrdenacaoInvalida() throws Exception {
        mockMvc.perform(get("/api/produtos").param("ordenacao", "QUALQUER"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verifyNoInteractions(buscarProdutos);
    }

    @Test
    void deveResponder400ParaPaginaNegativa() throws Exception {
        mockMvc.perform(get("/api/produtos").param("pagina", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("A página não pode ser negativa"));

        verifyNoInteractions(buscarProdutos);
    }

    @Test
    void deveListarDestaques() throws Exception {
        when(listarDestaques.listarDestaques(4)).thenReturn(List.of(controle));

        mockMvc.perform(get("/api/produtos/destaques").param("limite", "4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Controle PS4 sem fio Bluetooth"));
    }

    @Test
    void deveListarMaisVendidosComPromocaoPrecoNoPixEAvaliacoes() throws Exception {
        Produto emPromocao = new Produto(1L, "Controle PS4 sem fio Bluetooth", null, "Vibração dupla",
                new BigDecimal("149.90"), new BigDecimal("199.90"), 3, true, new Categoria(3L, "Games", null),
                List.of());
        when(listarDestaques.listarMaisVendidos(4)).thenReturn(List.of(emPromocao));
        when(consultarAvaliacoes.resumosDosProdutos(List.of(1L)))
                .thenReturn(Map.of(1L, new ResumoAvaliacoes(new BigDecimal("4.67"), 3)));

        mockMvc.perform(get("/api/produtos/mais-vendidos").param("limite", "4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].preco").value(149.9))
                .andExpect(jsonPath("$[0].precoAnterior").value(199.9))
                .andExpect(jsonPath("$[0].percentualDesconto").value(25))
                .andExpect(jsonPath("$[0].precoPix").value(142.4))
                .andExpect(jsonPath("$[0].notaMedia").value(4.7))
                .andExpect(jsonPath("$[0].totalAvaliacoes").value(3));
    }

    @Test
    void deveFiltrarSoAsOfertas() throws Exception {
        FiltroProdutos filtro = new FiltroProdutos(null, null, OrdenacaoProduto.MAIS_RECENTES, true);
        when(buscarProdutos.buscar(filtro, new Paginacao(0, 12)))
                .thenReturn(new Pagina<>(List.of(controle), 0, 12, 1));

        mockMvc.perform(get("/api/produtos").param("ofertas", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itens[0].slug").value("controle-ps4-sem-fio-bluetooth"))
                .andExpect(jsonPath("$.itens[0].precoAnterior").doesNotExist())
                .andExpect(jsonPath("$.itens[0].totalAvaliacoes").value(0));
    }
}
