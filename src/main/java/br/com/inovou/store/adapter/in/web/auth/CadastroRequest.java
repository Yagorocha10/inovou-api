package br.com.inovou.store.adapter.in.web.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CadastroRequest(
        @NotBlank(message = "Informe seu nome")
        @Size(min = 2, max = 120, message = "O nome deve ter entre 2 e 120 caracteres")
        String nome,
        @NotBlank(message = "Informe seu e-mail")
        @Email(message = "E-mail inválido")
        @Size(max = 160, message = "O e-mail pode ter no máximo 160 caracteres")
        String email,
        @NotBlank(message = "Informe uma senha")
        @Size(min = 8, max = 72, message = "A senha deve ter entre 8 e 72 caracteres")
        String senha) {
}
