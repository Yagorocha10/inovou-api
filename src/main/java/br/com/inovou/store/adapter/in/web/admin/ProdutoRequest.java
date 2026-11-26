package br.com.inovou.store.adapter.in.web.admin;

import br.com.inovou.store.application.consulta.DadosProduto;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProdutoRequest(
        @NotBlank(message = "Informe o nome do produto")
        @Size(max = 150, message = "O nome pode ter no máximo 150 caracteres")
        String nome,
        @Size(max = 2000, message = "A descrição pode ter no máximo 2000 caracteres")
        String descricao,
        @NotNull(message = "Informe o preço")
        @Positive(message = "O preço deve ser maior que zero")
        @Digits(integer = 8, fraction = 2, message = "O preço pode ter no máximo 2 casas decimais")
        BigDecimal preco,
        @NotNull(message = "Informe o estoque")
        @PositiveOrZero(message = "O estoque não pode ser negativo")
        Integer estoque,
        @NotNull(message = "Escolha uma categoria")
        Long categoriaId,
        boolean ativo) {

    DadosProduto paraDados() {
        return new DadosProduto(nome, descricao, preco, estoque, categoriaId, ativo);
    }
}
