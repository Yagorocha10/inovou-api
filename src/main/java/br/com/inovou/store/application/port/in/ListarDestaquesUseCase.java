package br.com.inovou.store.application.port.in;

import br.com.inovou.store.domain.produto.Produto;

import java.util.List;

public interface ListarDestaquesUseCase {

    List<Produto> listarDestaques(int limite);

    List<Produto> listarMaisVendidos(int limite);
}
