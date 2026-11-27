package br.com.inovou.store.application.consulta;

public record FiltroProdutosAdmin(String busca, String categoria, SituacaoProduto situacao) {

    public FiltroProdutosAdmin {
        busca = busca == null ? "" : busca.strip();
        categoria = categoria == null ? "" : categoria.strip();
        situacao = situacao == null ? SituacaoProduto.TODOS : situacao;
    }
}
