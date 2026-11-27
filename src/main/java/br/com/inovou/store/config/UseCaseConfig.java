package br.com.inovou.store.config;

import br.com.inovou.store.application.port.in.GerenciarPedidosUseCase;
import br.com.inovou.store.application.port.out.ArmazenamentoImagemPort;
import br.com.inovou.store.application.port.out.AvaliacaoRepositoryPort;
import br.com.inovou.store.application.port.out.CarrinhoRepositoryPort;
import br.com.inovou.store.application.port.out.CategoriaRepositoryPort;
import br.com.inovou.store.application.port.out.EnderecoRepositoryPort;
import br.com.inovou.store.application.port.out.PagamentoPort;
import br.com.inovou.store.application.port.out.PedidoRepositoryPort;
import br.com.inovou.store.application.port.out.ProdutoRepositoryPort;
import br.com.inovou.store.application.port.out.SenhaEncoderPort;
import br.com.inovou.store.application.port.out.TokenPort;
import br.com.inovou.store.application.port.out.TransacaoPort;
import br.com.inovou.store.application.port.out.UsuarioRepositoryPort;
import br.com.inovou.store.application.service.AutenticacaoService;
import br.com.inovou.store.application.service.AvaliacaoService;
import br.com.inovou.store.application.service.CarrinhoService;
import br.com.inovou.store.application.service.CatalogoService;
import br.com.inovou.store.application.service.CategoriaService;
import br.com.inovou.store.application.service.EnderecoService;
import br.com.inovou.store.application.service.PainelAdminService;
import br.com.inovou.store.application.service.PedidoAdminService;
import br.com.inovou.store.application.service.PedidoService;
import br.com.inovou.store.application.service.ProdutoAdminService;
import br.com.inovou.store.application.service.UsuarioService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

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

    @Bean
    CarrinhoService carrinhoService(CarrinhoRepositoryPort carrinhoRepository,
                                    ProdutoRepositoryPort produtoRepository) {
        return new CarrinhoService(carrinhoRepository, produtoRepository);
    }

    @Bean
    Clock relogio() {
        return Clock.system(ZoneId.of("America/Belem"));
    }

    @Bean
    PedidoService pedidoService(PedidoRepositoryPort pedidoRepository, CarrinhoRepositoryPort carrinhoRepository,
                                EnderecoRepositoryPort enderecoRepository, ProdutoRepositoryPort produtoRepository,
                                PagamentoPort pagamento, TransacaoPort transacao, Clock relogio) {
        return new PedidoService(pedidoRepository, carrinhoRepository, enderecoRepository, produtoRepository,
                pagamento, transacao, relogio);
    }

    @Bean
    ProdutoAdminService produtoAdminService(ProdutoRepositoryPort produtoRepository,
                                            CategoriaRepositoryPort categoriaRepository,
                                            ArmazenamentoImagemPort armazenamento) {
        return new ProdutoAdminService(produtoRepository, categoriaRepository, armazenamento);
    }

    @Bean
    PedidoAdminService pedidoAdminService(PedidoRepositoryPort pedidoRepository, UsuarioRepositoryPort usuarioRepository,
                                          ProdutoRepositoryPort produtoRepository, TransacaoPort transacao) {
        return new PedidoAdminService(pedidoRepository, usuarioRepository, produtoRepository, transacao);
    }

    @Bean
    AvaliacaoService avaliacaoService(AvaliacaoRepositoryPort avaliacaoRepository,
                                      ProdutoRepositoryPort produtoRepository, PedidoRepositoryPort pedidoRepository,
                                      UsuarioRepositoryPort usuarioRepository, Clock relogio) {
        return new AvaliacaoService(avaliacaoRepository, produtoRepository, pedidoRepository, usuarioRepository,
                relogio);
    }

    @Bean
    PainelAdminService painelAdminService(PedidoRepositoryPort pedidoRepository, ProdutoRepositoryPort produtoRepository,
                                          GerenciarPedidosUseCase pedidos, Clock relogio) {
        return new PainelAdminService(pedidoRepository, produtoRepository, pedidos, relogio);
    }
}
