package br.com.inovou.store.adapter.out.pagamento;

import br.com.inovou.store.domain.pedido.EnderecoEntrega;
import br.com.inovou.store.domain.pedido.FormaPagamento;
import br.com.inovou.store.domain.pedido.ItemPedido;
import br.com.inovou.store.domain.pedido.Pagamento;
import br.com.inovou.store.domain.pedido.Pedido;
import br.com.inovou.store.domain.pedido.StatusPedido;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PagamentoLojaAdapterTest {

    private Pedido pedido(FormaPagamento forma) {
        return new Pedido(4L, "INV-000004", 1L, StatusPedido.AGUARDANDO_PAGAMENTO, LocalDateTime.now(),
                List.of(new ItemPedido(1L, "Console", "console", null, new BigDecimal("40.00"), 2)),
                new BigDecimal("10.00"),
                new EnderecoEntrega("66010-000", "Rua A", "1", null, "Campina", "Belém", "PA"),
                Pagamento.pendente(forma));
    }

    @Test
    void comChaveConfiguradaGeraPixRealComOValorDoPedido() {
        PagamentoLojaAdapter adapter = new PagamentoLojaAdapter("chave-teste@inovou.local", "Inovou Store", "Belem");

        Pagamento pagamento = adapter.iniciar(pedido(FormaPagamento.PIX));

        assertThat(pagamento.aprovado()).isFalse();
        assertThat(pagamento.codigoPix()).startsWith("000201").contains("br.gov.bcb.pix")
                .contains("chave-teste@inovou.local").contains("540590.00").contains("INV000004");
        assertThat(pagamento.instrucoes()).contains("R$ 90,00");
    }

    @Test
    void semChaveContinuaSimuladoEAvisa() {
        PagamentoLojaAdapter adapter = new PagamentoLojaAdapter("", "Inovou Store", "Belem");

        Pagamento pagamento = adapter.iniciar(pedido(FormaPagamento.PIX));

        assertThat(pagamento.codigoPix()).startsWith("PIX-SIMULADO");
        assertThat(pagamento.instrucoes()).contains("simulação");
    }

    @Test
    void cartaoEPagamentoNaEntregaNaoMudam() {
        PagamentoLojaAdapter adapter = new PagamentoLojaAdapter("chave", "Inovou Store", "Belem");

        assertThat(adapter.iniciar(pedido(FormaPagamento.CARTAO)).aprovado()).isTrue();
        assertThat(adapter.iniciar(pedido(FormaPagamento.NA_ENTREGA)).codigoPix()).isNull();
    }
}
