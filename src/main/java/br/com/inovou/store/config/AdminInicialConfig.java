package br.com.inovou.store.config;

import br.com.inovou.store.application.port.out.SenhaEncoderPort;
import br.com.inovou.store.application.port.out.UsuarioRepositoryPort;
import br.com.inovou.store.domain.usuario.Usuario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AdminInicialConfig {

    private static final Logger LOG = LoggerFactory.getLogger(AdminInicialConfig.class);

    @Bean
    ApplicationRunner criarAdminInicial(UsuarioRepositoryPort usuarioRepository, SenhaEncoderPort senhaEncoder,
                                        @Value("${inovou.admin.email}") String email,
                                        @Value("${inovou.admin.senha}") String senha) {
        return argumentos -> {
            String emailNormalizado = Usuario.normalizarEmail(email);
            if (usuarioRepository.existePorEmail(emailNormalizado)) {
                return;
            }
            usuarioRepository.salvar(Usuario.novoAdmin("Administrador", emailNormalizado, senhaEncoder.codificar(senha)));
            LOG.info("Usuário administrador criado: {}", emailNormalizado);
        };
    }
}
