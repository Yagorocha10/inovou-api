package br.com.inovou.store.domain.frete;

import br.com.inovou.store.domain.exception.DomainException;
import br.com.inovou.store.domain.exception.EntregaIndisponivelException;

import java.math.BigDecimal;
import java.util.List;

public final class TabelaFrete {

    private record Faixa(int cepInicial, int cepFinal, Frete frete) {

        boolean atende(int cep) {
            return cep >= cepInicial && cep <= cepFinal;
        }
    }

    private static final List<Faixa> FAIXAS = List.of(
            new Faixa(66_000_000, 66_999_999, new Frete("Belém", new BigDecimal("10.00"), 2)),
            new Faixa(67_000_000, 67_299_999, new Frete("Região metropolitana", new BigDecimal("15.00"), 3)),
            new Faixa(68_740_000, 68_749_999, new Frete("Região metropolitana", new BigDecimal("15.00"), 3)),
            new Faixa(68_790_000, 68_799_999, new Frete("Região metropolitana", new BigDecimal("15.00"), 3)));

    private TabelaFrete() {
    }

    public static Frete calcular(String cep) {
        String digitos = cep == null ? "" : cep.replaceAll("\\D", "");
        if (digitos.length() != 8) {
            throw new DomainException("O CEP deve ter 8 dígitos");
        }
        int numero = Integer.parseInt(digitos);
        return FAIXAS.stream()
                .filter(faixa -> faixa.atende(numero))
                .map(Faixa::frete)
                .findFirst()
                .orElseThrow(() -> new EntregaIndisponivelException(cep));
    }
}
