package br.com.inovou.store.domain.pedido;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class DescontoPix {

    public static final int PERCENTUAL = 5;

    private static final BigDecimal FATOR = BigDecimal.valueOf(PERCENTUAL).movePointLeft(2);

    private DescontoPix() {
    }

    public static BigDecimal sobre(BigDecimal valor) {
        if (valor == null || valor.signum() <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return valor.multiply(FATOR).setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal precoNoPix(BigDecimal preco) {
        return preco.subtract(sobre(preco)).setScale(2, RoundingMode.HALF_UP);
    }
}
