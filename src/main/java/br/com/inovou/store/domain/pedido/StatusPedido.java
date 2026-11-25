package br.com.inovou.store.domain.pedido;

import java.util.Set;

public enum StatusPedido {

    AGUARDANDO_PAGAMENTO("Aguardando pagamento"),
    PAGO("Pago"),
    EM_SEPARACAO("Em separação"),
    SAIU_PARA_ENTREGA("Saiu para entrega"),
    ENTREGUE("Entregue"),
    CANCELADO("Cancelado");

    private final String descricao;

    StatusPedido(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    public Set<StatusPedido> proximosPermitidos() {
        return switch (this) {
            case AGUARDANDO_PAGAMENTO -> Set.of(PAGO, CANCELADO);
            case PAGO -> Set.of(EM_SEPARACAO, CANCELADO);
            case EM_SEPARACAO -> Set.of(SAIU_PARA_ENTREGA, CANCELADO);
            case SAIU_PARA_ENTREGA -> Set.of(ENTREGUE);
            case ENTREGUE, CANCELADO -> Set.of();
        };
    }

    public boolean podeIrPara(StatusPedido destino) {
        return proximosPermitidos().contains(destino);
    }

    public boolean clientePodeCancelar() {
        return this == AGUARDANDO_PAGAMENTO || this == PAGO;
    }
}
