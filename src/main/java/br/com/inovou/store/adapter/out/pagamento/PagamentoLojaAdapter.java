package br.com.inovou.store.adapter.out.pagamento;

import br.com.inovou.store.application.port.out.PagamentoPort;
import br.com.inovou.store.domain.pedido.FormaPagamento;
import br.com.inovou.store.domain.pedido.Pagamento;
import br.com.inovou.store.domain.pedido.Pedido;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.RoundingMode;

@Component
public class PagamentoLojaAdapter implements PagamentoPort {

    private final String chavePix;
    private final String nomeRecebedor;
    private final String cidade;

    public PagamentoLojaAdapter(@Value("${inovou.pix.chave:}") String chavePix,
                                @Value("${inovou.pix.nome-recebedor:INOVOU STORE}") String nomeRecebedor,
                                @Value("${inovou.pix.cidade:BELEM}") String cidade) {
        this.chavePix = chavePix == null ? "" : chavePix.strip();
        this.nomeRecebedor = nomeRecebedor;
        this.cidade = cidade;
    }

    @Override
    public Pagamento iniciar(Pedido pedido) {
        FormaPagamento forma = pedido.getPagamento().forma();
        return switch (forma) {
            case PIX -> new Pagamento(forma, false, instrucoesPix(pedido), codigoPix(pedido));
            case CARTAO -> new Pagamento(forma, true,
                    "Pagamento aprovado no cartão (ambiente de simulação, nenhuma cobrança foi feita).", null);
            case NA_ENTREGA -> new Pagamento(forma, false,
                    "Pague na entrega em dinheiro, Pix ou cartão. Combinaremos o horário pelo WhatsApp.", null);
        };
    }

    boolean pixConfigurado() {
        return !chavePix.isEmpty();
    }

    private String instrucoesPix(Pedido pedido) {
        if (!pixConfigurado()) {
            return "Pix em modo de simulação: este código não pode ser pago. Configure a chave Pix da loja.";
        }
        return "Abra o app do seu banco, escolha Pix copia e cola (ou leia o QR Code) e pague R$ %s. "
                .formatted(pedido.total().setScale(2, RoundingMode.HALF_UP).toPlainString().replace('.', ','))
                + "Assim que confirmarmos o recebimento, começamos a separar seu pedido.";
    }

    private String codigoPix(Pedido pedido) {
        if (!pixConfigurado()) {
            String valor = pedido.total().setScale(2, RoundingMode.HALF_UP).toPlainString();
            return "PIX-SIMULADO.INOVOU-STORE.%s.%s".formatted(pedido.getCodigo(), valor);
        }
        return PixCopiaECola.gerar(chavePix, nomeRecebedor, cidade, pedido.total(), pedido.getCodigo());
    }
}
