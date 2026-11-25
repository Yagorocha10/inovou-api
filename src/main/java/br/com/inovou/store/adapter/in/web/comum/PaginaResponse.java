package br.com.inovou.store.adapter.in.web.comum;

import br.com.inovou.store.application.consulta.Pagina;

import java.util.List;
import java.util.function.Function;

public record PaginaResponse<T>(List<T> itens, int pagina, int tamanho, long totalItens, int totalPaginas) {

    public static <D, T> PaginaResponse<T> de(Pagina<D> pagina, Function<D, T> conversor) {
        Pagina<T> convertida = pagina.map(conversor);
        return new PaginaResponse<>(convertida.itens(), convertida.pagina(), convertida.tamanho(),
                convertida.totalItens(), convertida.totalPaginas());
    }
}
