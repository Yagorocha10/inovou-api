package br.com.inovou.store.config;

import br.com.inovou.store.application.port.out.CategoriaRepositoryPort;
import br.com.inovou.store.application.port.out.EnderecoRepositoryPort;
import br.com.inovou.store.application.port.out.ProdutoRepositoryPort;
import br.com.inovou.store.application.port.out.SenhaEncoderPort;
import br.com.inovou.store.application.port.out.TokenPort;
import br.com.inovou.store.application.port.out.UsuarioRepositoryPort;
import br.com.inovou.store.application.service.AutenticacaoService;
import br.com.inovou.store.application.service.CatalogoService;
import br.com.inovou.store.application.service.CategoriaService;
import br.com.inovou.store.application.service.EnderecoService;
import br.com.inovou.store.application.service.UsuarioService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

    @Bean
    CatalogoService catalogoService(ProdutoRepositoryPort produtoRepository) {
        return new CatalogoService(produtoRepository);
    }

    @Bean
    CategoriaService categoriaService(CategoriaRepositoryPort categoriaRepository) {
        return new CategoriaService(categoriaRepository);
    }

    @Bean
    AutenticacaoService autenticacaoService(UsuarioRepositoryPort usuarioRepository, SenhaEncoderPort senhaEncoder,
                                            TokenPort tokenPort) {
        return new AutenticacaoService(usuarioRepository, senhaEncoder, tokenPort);
    }

    @Bean
    UsuarioService usuarioService(UsuarioRepositoryPort usuarioRepository) {
        return new UsuarioService(usuarioRepository);
    }

    @Bean
    EnderecoService enderecoService(EnderecoRepositoryPort enderecoRepository) {
        return new EnderecoService(enderecoRepository);
    }
}
