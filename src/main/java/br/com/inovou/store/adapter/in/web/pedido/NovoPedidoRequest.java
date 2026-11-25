package br.com.inovou.store.adapter.in.web.pedido;

import br.com.inovou.store.domain.pedido.FormaPagamento;
import jakarta.validation.constraints.NotNull;

public record NovoPedidoRequest(
        @NotNull(message = "Escolha um endereço de entrega") Long enderecoId,
        @NotNull(message = "Escolha uma forma de pagamento") FormaPagamento formaPagamento) {
}
