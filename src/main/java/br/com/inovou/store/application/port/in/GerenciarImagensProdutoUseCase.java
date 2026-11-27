package br.com.inovou.store.application.port.in;

import br.com.inovou.store.application.consulta.ArquivoImagem;
import br.com.inovou.store.domain.produto.Produto;

import java.util.List;

public interface GerenciarImagensProdutoUseCase {

    Produto adicionar(Long produtoId, ArquivoImagem arquivo);

    Produto remover(Long produtoId, Long imagemId);

    Produto reordenar(Long produtoId, List<Long> idsNaNovaOrdem);
}
