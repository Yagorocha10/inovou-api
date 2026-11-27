package br.com.inovou.store.adapter.in.web.admin;

import br.com.inovou.store.domain.pedido.StatusPedido;
import jakarta.validation.constraints.NotNull;

public record StatusRequest(@NotNull(message = "Informe o novo status") StatusPedido status) {
}
