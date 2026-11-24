package br.com.inovou.store.adapter.out.seguranca;

import br.com.inovou.store.application.port.out.SenhaEncoderPort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class BCryptSenhaAdapter implements SenhaEncoderPort {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Override
    public String codificar(String senhaPura) {
        return encoder.encode(senhaPura);
    }

    @Override
    public boolean confere(String senhaPura, String senhaCodificada) {
        return encoder.matches(senhaPura, senhaCodificada);
    }
}
