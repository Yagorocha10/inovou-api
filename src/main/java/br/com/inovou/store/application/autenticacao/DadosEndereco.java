package br.com.inovou.store.application.autenticacao;

public record DadosEndereco(
        String cep,
        String logradouro,
        String numero,
        String complemento,
        String bairro,
        String cidade,
        String uf) {
}
