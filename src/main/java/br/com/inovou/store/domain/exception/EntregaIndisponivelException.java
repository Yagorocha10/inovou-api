package br.com.inovou.store.domain.exception;

public class EntregaIndisponivelException extends DomainException {

    public EntregaIndisponivelException(String cep) {
        super("Ainda não entregamos no CEP %s. Atendemos Belém e região metropolitana.".formatted(cep));
    }
}
