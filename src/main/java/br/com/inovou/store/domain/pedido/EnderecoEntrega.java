package br.com.inovou.store.domain.pedido;

import br.com.inovou.store.domain.usuario.Endereco;

public record EnderecoEntrega(String cep, String logradouro, String numero, String complemento,
                              String bairro, String cidade, String uf) {

    public static EnderecoEntrega de(Endereco endereco) {
        return new EnderecoEntrega(endereco.cep(), endereco.logradouro(), endereco.numero(),
                endereco.complemento(), endereco.bairro(), endereco.cidade(), endereco.uf());
    }
}
