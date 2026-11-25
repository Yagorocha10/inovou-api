package br.com.inovou.store.adapter.in.web.carrinho;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AlterarQuantidadeRequest(
        @NotNull(message = "Informe a quantidade")
        @Min(value = 1, message = "A quantidade deve ser maior que zero")
        @Max(value = 99, message = "A quantidade máxima é 99")
        Integer quantidade) {
}
