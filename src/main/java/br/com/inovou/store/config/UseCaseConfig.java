package br.com.inovou.store.config;

import br.com.inovou.store.application.port.out.CategoriaRepositoryPort;
import br.com.inovou.store.application.port.out.ProdutoRepositoryPort;
import br.com.inovou.store.application.service.CatalogoService;
import br.com.inovou.store.application.service.CategoriaService;
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
}
