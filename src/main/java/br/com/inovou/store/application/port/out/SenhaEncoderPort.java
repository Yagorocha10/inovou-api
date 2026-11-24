package br.com.inovou.store.application.port.out;

public interface SenhaEncoderPort {

    String codificar(String senhaPura);

    boolean confere(String senhaPura, String senhaCodificada);
}
