package br.com.inovou.store.application.autenticacao;

import java.time.Instant;

public record TokenEmitido(String valor, Instant expiraEm) {
}
