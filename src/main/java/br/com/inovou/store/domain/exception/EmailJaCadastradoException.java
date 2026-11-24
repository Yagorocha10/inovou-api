package br.com.inovou.store.domain.exception;

public class EmailJaCadastradoException extends DomainException {

    public EmailJaCadastradoException(String email) {
        super("Já existe uma conta com o e-mail " + email);
    }
}
