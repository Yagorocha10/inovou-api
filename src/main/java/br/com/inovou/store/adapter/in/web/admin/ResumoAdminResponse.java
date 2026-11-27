package br.com.inovou.store.adapter.in.web.admin;

import br.com.inovou.store.application.consulta.ResumoAdmin;

import java.math.BigDecimal;
import java.util.List;

public record ResumoAdminResponse(long pedidosHoje, BigDecimal faturamentoHoje, long pedidosParaPreparar,
                                  long produtosSemEstoque, List<PedidoAdminResumoResponse> ultimosPedidos) {

    static ResumoAdminResponse de(ResumoAdmin resumo) {
        return new ResumoAdminResponse(resumo.pedidosHoje(), resumo.faturamentoHoje(), resumo.pedidosParaPreparar(),
                resumo.produtosSemEstoque(),
                resumo.ultimosPedidos().stream().map(PedidoAdminResumoResponse::de).toList());
    }
}
