package br.com.inovou.store.application.service;

import br.com.inovou.store.application.consulta.Pagina;
import br.com.inovou.store.application.consulta.ResumoAdmin;
import br.com.inovou.store.application.port.in.GerenciarPedidosUseCase;
import br.com.inovou.store.application.port.out.PedidoRepositoryPort;
import br.com.inovou.store.application.port.out.ProdutoRepositoryPort;
import br.com.inovou.store.domain.pedido.StatusPedido;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.EnumSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PainelAdminServiceTest {

    @Mock
    private PedidoRepositoryPort pedidoRepository;
    @Mock
    private ProdutoRepositoryPort produtoRepository;
    @Mock
    private GerenciarPedidosUseCase pedidos;

    @Test
    void hojeEhODiaDeBelemMesmoQuandoEmUtcJaVirouODia() {
        Clock umaDaManhaUtc = Clock.fixed(Instant.parse("2026-11-28T01:30:00Z"), ZoneId.of("America/Belem"));
        PainelAdminService service = new PainelAdminService(pedidoRepository, produtoRepository, pedidos, umaDaManhaUtc);
        LocalDateTime inicio = LocalDateTime.of(2026, 11, 27, 0, 0);
        LocalDateTime fim = LocalDateTime.of(2026, 11, 28, 0, 0);
        when(pedidoRepository.contarCriadosEntre(inicio, fim)).thenReturn(4L);
        when(pedidoRepository.somarTotalNaoCanceladoEntre(inicio, fim)).thenReturn(new BigDecimal("359.70"));
        when(pedidoRepository.contarPorStatus(EnumSet.of(StatusPedido.PAGO, StatusPedido.EM_SEPARACAO))).thenReturn(2L);
        when(produtoRepository.contarAtivosSemEstoque()).thenReturn(1L);
        when(pedidos.listar(any(), any())).thenReturn(new Pagina<>(List.of(), 0, 5, 0));

        ResumoAdmin resumo = service.resumo();

        assertThat(resumo.pedidosHoje()).isEqualTo(4);
        assertThat(resumo.faturamentoHoje()).isEqualByComparingTo("359.70");
        assertThat(resumo.pedidosParaPreparar()).isEqualTo(2);
        assertThat(resumo.produtosSemEstoque()).isEqualTo(1);
        assertThat(resumo.ultimosPedidos()).isEmpty();
    }
}
