package br.com.inovou.store.application.port.out;

import br.com.inovou.store.application.consulta.ArquivoImagem;

public interface ArmazenamentoImagemPort {

    String salvar(Long produtoId, ArquivoImagem arquivo);

    void remover(String url);
}
