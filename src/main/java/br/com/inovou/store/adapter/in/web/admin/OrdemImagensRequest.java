package br.com.inovou.store.adapter.in.web.admin;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record OrdemImagensRequest(@NotEmpty(message = "Envie os ids das fotos na nova ordem") List<Long> ids) {
}
