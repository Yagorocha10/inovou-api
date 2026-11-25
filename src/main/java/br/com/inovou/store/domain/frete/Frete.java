package br.com.inovou.store.domain.frete;

import java.math.BigDecimal;

public record Frete(String regiao, BigDecimal valor, int prazoDias) {
}
