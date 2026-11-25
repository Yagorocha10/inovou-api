package br.com.inovou.store.application.port.out;

import br.com.inovou.store.domain.pedido.Pagamento;
import br.com.inovou.store.domain.pedido.Pedido;

public interface PagamentoPort {

    Pagamento iniciar(Pedido pedido);
}
