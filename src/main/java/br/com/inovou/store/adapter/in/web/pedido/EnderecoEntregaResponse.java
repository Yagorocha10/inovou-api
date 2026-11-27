package br.com.inovou.store.adapter.in.web.pedido;

import br.com.inovou.store.domain.pedido.EnderecoEntrega;

public record EnderecoEntregaResponse(String cep, String logradouro, String numero, String complemento,
                                      String bairro, String cidade, String uf) {

    public static EnderecoEntregaResponse de(EnderecoEntrega endereco) {
        return new EnderecoEntregaResponse(endereco.cep(), endereco.logradouro(), endereco.numero(),
                endereco.complemento(), endereco.bairro(), endereco.cidade(), endereco.uf());
    }
}
