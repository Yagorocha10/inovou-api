package br.com.inovou.store.application.consulta;

public record PermissaoAvaliacao(boolean podeAvaliar, String motivo) {

    public static PermissaoAvaliacao permitida() {
        return new PermissaoAvaliacao(true, null);
    }

    public static PermissaoAvaliacao negada(String motivo) {
        return new PermissaoAvaliacao(false, motivo);
    }
}
