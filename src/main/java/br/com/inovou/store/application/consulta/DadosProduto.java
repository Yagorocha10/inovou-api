package br.com.inovou.store.application.consulta;

import java.math.BigDecimal;

public record DadosProduto(String nome, String descricao, BigDecimal preco, int estoque, Long categoriaId,
                           boolean ativo) {
}
