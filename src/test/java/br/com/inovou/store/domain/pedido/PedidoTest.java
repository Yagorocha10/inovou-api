package br.com.inovou.store.domain.pedido;

import br.com.inovou.store.domain.exception.DomainException;
import br.com.inovou.store.domain.exception.TransicaoInvalidaException;
import br.com.inovou.store.domain.frete.Frete;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PedidoTest {

    private final EnderecoEntrega endereco = new EnderecoEntrega("66000-000", "Rua A", "10", null, "Nazaré",
            "Belém", "PA");
    private final Frete freteBelem = new Frete("Belém", new BigDecimal("10.00"), 2);
    private final List<ItemPedido> itens = List.of(
            new ItemPedido(1L, "Controle", "controle", null, new BigDecimal("129.90"), 2),
            new ItemPedido(2L, "Cabo", "cabo", null, new BigDecimal("30.00"), 1));

    private Pedido novo(FormaPagamento forma) {
        return Pedido.novo(7L, itens, endereco, freteBelem, forma, LocalDateTime.of(2026, 11, 25, 10, 0));
    }

    @Test
    void pedidoNovoNasceAguardandoPagamentoComTotaisCalculados() {
        Pedido pedido = novo(FormaPagamento.PIX);

        assertThat(pedido.getStatus()).isEqualTo(StatusPedido.AGUARDANDO_PAGAMENTO);
        assertThat(pedido.subtotal()).isEqualByComparingTo("289.80");
        assertThat(pedido.total()).isEqualByComparingTo("299.80");
        assertThat(pedido.quantidadeItens()).isEqualTo(3);
        assertThat(pedido.podeSerCanceladoPeloCliente()).isTrue();
    }

    @Test
    void pagamentoAprovadoMarcaOPedidoComoPago() {
        Pedido pedido = novo(FormaPagamento.CARTAO);

        pedido.registrarPagamento(new Pagamento(FormaPagamento.CARTAO, true, "Aprovado", null));

        assertThat(pedido.getStatus()).isEqualTo(StatusPedido.PAGO);
    }

    @Test
    void pagamentoPendenteMantemAguardandoPagamento() {
        Pedido pedido = novo(FormaPagamento.PIX);

        pedido.registrarPagamento(new Pagamento(FormaPagamento.PIX, false, "Pague o Pix", "PIX-123"));

        assertThat(pedido.getStatus()).isEqualTo(StatusPedido.AGUARDANDO_PAGAMENTO);
        assertThat(pedido.getPagamento().codigoPix()).isEqualTo("PIX-123");
    }

    @Test
    void pagamentoDeOutraFormaEhRecusado() {
        Pedido pedido = novo(FormaPagamento.PIX);

        assertThatThrownBy(() -> pedido.registrarPagamento(new Pagamento(FormaPagamento.CARTAO, true, "", null)))
                .isInstanceOf(DomainException.class);
    }

    @Test
    void segueAOrdemDosStatusAteSerEntregue() {
        Pedido pedido = novo(FormaPagamento.CARTAO);

        pedido.avancarPara(StatusPedido.PAGO);
        pedido.avancarPara(StatusPedido.EM_SEPARACAO);
        pedido.avancarPara(StatusPedido.SAIU_PARA_ENTREGA);
        pedido.avancarPara(StatusPedido.ENTREGUE);

        assertThat(pedido.getStatus()).isEqualTo(StatusPedido.ENTREGUE);
        assertThat(pedido.podeSerCanceladoPeloCliente()).isFalse();
    }

    @Test
    void naoPulaEtapas() {
        Pedido pedido = novo(FormaPagamento.PIX);

        assertThatThrownBy(() -> pedido.avancarPara(StatusPedido.ENTREGUE))
                .isInstanceOf(TransicaoInvalidaException.class)
                .hasMessageContaining("Aguardando pagamento")
                .hasMessageContaining("Entregue");
    }

    @Test
    void clienteCancelaAntesDaSeparacaoMasNaoDepois() {
        Pedido aguardando = novo(FormaPagamento.PIX);
        aguardando.cancelarPeloCliente();
        assertThat(aguardando.getStatus()).isEqualTo(StatusPedido.CANCELADO);

        Pedido emSeparacao = novo(FormaPagamento.CARTAO);
        emSeparacao.avancarPara(StatusPedido.PAGO);
        emSeparacao.avancarPara(StatusPedido.EM_SEPARACAO);
        assertThatThrownBy(emSeparacao::cancelarPeloCliente).isInstanceOf(TransicaoInvalidaException.class);

        assertThatThrownBy(aguardando::cancelarPeloCliente).isInstanceOf(TransicaoInvalidaException.class);
    }

    @Test
    void naoExistePedidoSemItens() {
        assertThatThrownBy(() -> Pedido.novo(7L, List.of(), endereco, freteBelem, FormaPagamento.PIX,
                LocalDateTime.now())).isInstanceOf(DomainException.class);
    }
}
