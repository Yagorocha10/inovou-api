package br.com.inovou.store.adapter.in.web.pedido;

import br.com.inovou.store.application.consulta.ResumoCheckout;

import java.math.BigDecimal;
import java.util.List;

public record ResumoCheckoutResponse(List<ItemPedidoResponse> itens, BigDecimal subtotal, BigDecimal frete,
                                     BigDecimal total, BigDecimal descontoPix, BigDecimal totalPix,
                                     int prazoEntregaDias, EnderecoEntregaResponse endereco) {

    static ResumoCheckoutResponse de(ResumoCheckout resumo) {
        return new ResumoCheckoutResponse(
                resumo.itens().stream().map(ItemPedidoResponse::de).toList(),
                resumo.subtotal(),
                resumo.frete().valor(),
                resumo.total(),
                resumo.descontoPix(),
                resumo.totalNoPix(),
                resumo.frete().prazoDias(),
                EnderecoEntregaResponse.de(resumo.endereco()));
    }
}
