package br.com.inovou.store.adapter.out.pagamento;

import br.com.inovou.store.application.port.out.PagamentoPort;
import br.com.inovou.store.domain.pedido.FormaPagamento;
import br.com.inovou.store.domain.pedido.Pagamento;
import br.com.inovou.store.domain.pedido.Pedido;
import org.springframework.stereotype.Component;

import java.math.RoundingMode;

@Component
public class PagamentoSimuladoAdapter implements PagamentoPort {

    @Override
    public Pagamento iniciar(Pedido pedido) {
        FormaPagamento forma = pedido.getPagamento().forma();
        return switch (forma) {
            case PIX -> new Pagamento(forma, false,
                    "Pague com o código Pix abaixo. Assim que o pagamento for confirmado, começamos a separar seu pedido.",
                    codigoPix(pedido));
            case CARTAO -> new Pagamento(forma, true,
                    "Pagamento aprovado no cartão (ambiente de simulação, nenhuma cobrança foi feita).", null);
            case NA_ENTREGA -> new Pagamento(forma, false,
                    "Pague na entrega em dinheiro, Pix ou cartão. Combinaremos o horário pelo WhatsApp.", null);
        };
    }

    private static String codigoPix(Pedido pedido) {
        String valor = pedido.total().setScale(2, RoundingMode.HALF_UP).toPlainString();
        return "PIX-SIMULADO.INOVOU-STORE.%s.%s".formatted(pedido.getCodigo(), valor);
    }
}
