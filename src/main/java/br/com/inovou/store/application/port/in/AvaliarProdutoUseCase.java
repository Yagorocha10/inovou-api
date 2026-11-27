package br.com.inovou.store.application.port.in;

import br.com.inovou.store.application.consulta.PermissaoAvaliacao;
import br.com.inovou.store.domain.avaliacao.Avaliacao;

public interface AvaliarProdutoUseCase {

    Avaliacao avaliar(Long usuarioId, String slugProduto, int nota, String comentario);

    PermissaoAvaliacao permissao(Long usuarioId, String slugProduto);
}
