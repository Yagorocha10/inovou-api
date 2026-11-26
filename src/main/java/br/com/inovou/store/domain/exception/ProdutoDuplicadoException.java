package br.com.inovou.store.domain.exception;

public class ProdutoDuplicadoException extends DomainException {

    public ProdutoDuplicadoException(String nome) {
        super("Já existe um produto com o nome \"%s\"".formatted(nome));
    }
}
