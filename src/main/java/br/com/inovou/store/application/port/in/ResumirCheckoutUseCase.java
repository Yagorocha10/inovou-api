package br.com.inovou.store.application.port.in;

import br.com.inovou.store.application.consulta.ResumoCheckout;

public interface ResumirCheckoutUseCase {

    ResumoCheckout resumir(Long usuarioId, Long enderecoId);
}
