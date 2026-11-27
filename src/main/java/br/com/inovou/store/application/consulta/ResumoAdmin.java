package br.com.inovou.store.application.consulta;

import java.math.BigDecimal;
import java.util.List;

public record ResumoAdmin(long pedidosHoje, BigDecimal faturamentoHoje, long pedidosParaPreparar,
                          long produtosSemEstoque, List<PedidoComCliente> ultimosPedidos) {

    public ResumoAdmin {
        ultimosPedidos = List.copyOf(ultimosPedidos);
    }
}
