package br.com.inovou.store.domain.exception;

public class EstoqueInsuficienteException extends DomainException {

    public EstoqueInsuficienteException(String produto, int disponivel, int solicitado) {
        super("Estoque insuficiente para %s: disponível %d, solicitado %d"
                .formatted(produto, disponivel, solicitado));
    }
}
