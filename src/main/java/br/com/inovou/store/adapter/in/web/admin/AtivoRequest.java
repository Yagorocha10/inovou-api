package br.com.inovou.store.adapter.in.web.admin;

import jakarta.validation.constraints.NotNull;

public record AtivoRequest(@NotNull(message = "Informe se o produto fica ativo") Boolean ativo) {
}
