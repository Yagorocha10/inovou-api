package br.com.inovou.store.adapter.in.web.avaliacao;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AvaliacaoRequest(
        @NotNull(message = "Escolha uma nota de 1 a 5 estrelas")
        @Min(value = 1, message = "A nota deve ser de 1 a 5 estrelas")
        @Max(value = 5, message = "A nota deve ser de 1 a 5 estrelas")
        Integer nota,
        @Size(max = 1000, message = "O comentário pode ter no máximo 1000 caracteres")
        String comentario) {
}
