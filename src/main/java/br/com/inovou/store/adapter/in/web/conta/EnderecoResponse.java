package br.com.inovou.store.adapter.in.web.conta;

import br.com.inovou.store.domain.usuario.Endereco;

public record EnderecoResponse(
        Long id,
        String cep,
        String logradouro,
        String numero,
        String complemento,
        String bairro,
        String cidade,
        String uf) {

    public static EnderecoResponse de(Endereco endereco) {
        return new EnderecoResponse(endereco.id(), endereco.cep(), endereco.logradouro(), endereco.numero(),
                endereco.complemento(), endereco.bairro(), endereco.cidade(), endereco.uf());
    }
}
