package br.com.inovou.store.application.consulta;

public record FiltroProdutos(String busca, String categoria, OrdenacaoProduto ordenacao, boolean apenasOfertas) {

    public FiltroProdutos(String busca, String categoria, OrdenacaoProduto ordenacao) {
        this(busca, categoria, ordenacao, false);
    }

    public FiltroProdutos {
        busca = busca == null ? "" : busca.strip();
        categoria = categoria == null ? "" : categoria.strip();
        ordenacao = ordenacao == null ? OrdenacaoProduto.MAIS_RECENTES : ordenacao;
    }

    public static FiltroProdutos semFiltro() {
        return new FiltroProdutos(null, null, null);
    }
}
