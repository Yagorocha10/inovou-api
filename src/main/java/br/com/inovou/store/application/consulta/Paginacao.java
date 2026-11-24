package br.com.inovou.store.application.consulta;

import br.com.inovou.store.domain.exception.DomainException;

public record Paginacao(int pagina, int tamanho) {

    public static final int TAMANHO_PADRAO = 12;
    public static final int TAMANHO_MAXIMO = 48;

    public Paginacao {
        if (pagina < 0) {
            throw new DomainException("A página não pode ser negativa");
        }
        if (tamanho < 1 || tamanho > TAMANHO_MAXIMO) {
            throw new DomainException("O tamanho da página deve ficar entre 1 e %d".formatted(TAMANHO_MAXIMO));
        }
    }

    public static Paginacao de(Integer pagina, Integer tamanho) {
        return new Paginacao(pagina == null ? 0 : pagina, tamanho == null ? TAMANHO_PADRAO : tamanho);
    }
}
