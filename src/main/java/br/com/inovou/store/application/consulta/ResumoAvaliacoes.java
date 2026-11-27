package br.com.inovou.store.application.consulta;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record ResumoAvaliacoes(BigDecimal media, long total) {

    public ResumoAvaliacoes {
        media = (media == null || total == 0) ? null : media.setScale(1, RoundingMode.HALF_UP);
    }

    public static ResumoAvaliacoes vazio() {
        return new ResumoAvaliacoes(null, 0);
    }
}
