package br.com.inovou.store.adapter.in.web.comum;

import java.time.OffsetDateTime;
import java.util.Map;

public record ErroResponse(int status, String mensagem, Map<String, String> campos, String instante) {

    public static ErroResponse de(int status, String mensagem) {
        return de(status, mensagem, Map.of());
    }

    public static ErroResponse de(int status, String mensagem, Map<String, String> campos) {
        return new ErroResponse(status, mensagem, campos, OffsetDateTime.now().toString());
    }
}
