package br.com.inovou.store.domain.pedido;

public record Pagamento(FormaPagamento forma, boolean aprovado, String instrucoes, String codigoPix) {

    public static Pagamento pendente(FormaPagamento forma) {
        return new Pagamento(forma, false, "", null);
    }
}
