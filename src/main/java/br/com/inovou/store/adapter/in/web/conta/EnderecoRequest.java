package br.com.inovou.store.adapter.in.web.conta;

import br.com.inovou.store.application.autenticacao.DadosEndereco;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record EnderecoRequest(
        @NotBlank(message = "Informe o CEP")
        @Pattern(regexp = "\\d{5}-?\\d{3}", message = "CEP inválido")
        String cep,
        @NotBlank(message = "Informe o logradouro") @Size(max = 150) String logradouro,
        @NotBlank(message = "Informe o número") @Size(max = 20) String numero,
        @Size(max = 80) String complemento,
        @NotBlank(message = "Informe o bairro") @Size(max = 80) String bairro,
        @NotBlank(message = "Informe a cidade") @Size(max = 80) String cidade,
        @NotBlank(message = "Informe a UF") @Size(min = 2, max = 2, message = "UF inválida") String uf) {

    public DadosEndereco paraDados() {
        return new DadosEndereco(cep, logradouro, numero, complemento, bairro, cidade, uf);
    }
}
