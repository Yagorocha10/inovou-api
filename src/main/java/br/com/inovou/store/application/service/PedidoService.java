package br.com.inovou.store.application.service;

import br.com.inovou.store.application.consulta.Pagina;
import br.com.inovou.store.application.consulta.Paginacao;
import br.com.inovou.store.application.consulta.ResumoCheckout;
import br.com.inovou.store.application.port.in.CancelarPedidoUseCase;
import br.com.inovou.store.application.port.in.ConsultarPedidosUseCase;
import br.com.inovou.store.application.port.in.CriarPedidoUseCase;
import br.com.inovou.store.application.port.in.ResumirCheckoutUseCase;
import br.com.inovou.store.application.port.out.CarrinhoRepositoryPort;
import br.com.inovou.store.application.port.out.EnderecoRepositoryPort;
import br.com.inovou.store.application.port.out.PagamentoPort;
import br.com.inovou.store.application.port.out.PedidoRepositoryPort;
import br.com.inovou.store.application.port.out.ProdutoRepositoryPort;
import br.com.inovou.store.application.port.out.TransacaoPort;
import br.com.inovou.store.domain.carrinho.Carrinho;
import br.com.inovou.store.domain.carrinho.ItemCarrinho;
import br.com.inovou.store.domain.exception.DomainException;
import br.com.inovou.store.domain.exception.EstoqueInsuficienteException;
import br.com.inovou.store.domain.exception.RecursoNaoEncontradoException;
import br.com.inovou.store.domain.frete.Frete;
import br.com.inovou.store.domain.frete.TabelaFrete;
import br.com.inovou.store.domain.pedido.EnderecoEntrega;
import br.com.inovou.store.domain.pedido.FormaPagamento;
import br.com.inovou.store.domain.pedido.ItemPedido;
import br.com.inovou.store.domain.pedido.Pedido;
import br.com.inovou.store.domain.produto.Produto;
import br.com.inovou.store.domain.usuario.Endereco;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

public class PedidoService implements ResumirCheckoutUseCase, CriarPedidoUseCase, ConsultarPedidosUseCase,
        CancelarPedidoUseCase {

    private final PedidoRepositoryPort pedidoRepository;
    private final CarrinhoRepositoryPort carrinhoRepository;
    private final EnderecoRepositoryPort enderecoRepository;
    private final ProdutoRepositoryPort produtoRepository;
    private final PagamentoPort pagamento;
    private final TransacaoPort transacao;
    private final Clock relogio;

    public PedidoService(PedidoRepositoryPort pedidoRepository, CarrinhoRepositoryPort carrinhoRepository,
                         EnderecoRepositoryPort enderecoRepository, ProdutoRepositoryPort produtoRepository,
                         PagamentoPort pagamento, TransacaoPort transacao, Clock relogio) {
        this.pedidoRepository = pedidoRepository;
        this.carrinhoRepository = carrinhoRepository;
        this.enderecoRepository = enderecoRepository;
        this.produtoRepository = produtoRepository;
        this.pagamento = pagamento;
        this.transacao = transacao;
        this.relogio = relogio;
    }

    @Override
    public ResumoCheckout resumir(Long usuarioId, Long enderecoId) {
        Endereco endereco = enderecoDoUsuario(usuarioId, enderecoId);
        Frete frete = TabelaFrete.calcular(endereco.cep());
        Carrinho carrinho = carrinhoParaFinalizar(usuarioId);
        return new ResumoCheckout(itensDoPedido(carrinho), frete, EnderecoEntrega.de(endereco));
    }

    @Override
    public Pedido criar(Long usuarioId, Long enderecoId, FormaPagamento formaPagamento) {
        if (formaPagamento == null) {
            throw new DomainException("Escolha uma forma de pagamento");
        }
        return transacao.executar(() -> {
            Endereco endereco = enderecoDoUsuario(usuarioId, enderecoId);
            Frete frete = TabelaFrete.calcular(endereco.cep());
            Carrinho carrinho = carrinhoParaFinalizar(usuarioId);
            List<ItemPedido> itens = itensDoPedido(carrinho);

            baixarEstoque(carrinho);

            Pedido pedido = pedidoRepository.salvar(Pedido.novo(usuarioId, itens, EnderecoEntrega.de(endereco),
                    frete, formaPagamento, LocalDateTime.now(relogio)));
            pedido.registrarPagamento(pagamento.iniciar(pedido));
            Pedido salvo = pedidoRepository.salvar(pedido);

            carrinho.esvaziar();
            carrinhoRepository.salvar(carrinho);
            return salvo;
        });
    }

    @Override
    public Pagina<Pedido> listar(Long usuarioId, Paginacao paginacao) {
        return pedidoRepository.listarDoUsuario(usuarioId, paginacao);
    }

    @Override
    public Pedido buscar(Long usuarioId, Long pedidoId) {
        return pedidoRepository.buscarPorId(pedidoId)
                .filter(pedido -> pedido.pertenceA(usuarioId))
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pedido não encontrado"));
    }

    @Override
    public Pedido cancelar(Long usuarioId, Long pedidoId) {
        return transacao.executar(() -> {
            Pedido pedido = buscar(usuarioId, pedidoId);
            pedido.cancelarPeloCliente();
            pedido.getItens().forEach(item -> produtoRepository.reporEstoque(item.produtoId(), item.quantidade()));
            return pedidoRepository.salvar(pedido);
        });
    }

    private Endereco enderecoDoUsuario(Long usuarioId, Long enderecoId) {
        if (enderecoId == null) {
            throw new DomainException("Escolha um endereço de entrega");
        }
        return enderecoRepository.buscarDoUsuario(enderecoId, usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Endereço não encontrado"));
    }

    private Carrinho carrinhoParaFinalizar(Long usuarioId) {
        Carrinho carrinho = carrinhoRepository.buscarDoUsuario(usuarioId)
                .filter(encontrado -> !encontrado.estaVazio())
                .orElseThrow(() -> new DomainException("Seu carrinho está vazio"));
        for (ItemCarrinho item : carrinho.getItens()) {
            if (!item.podeSerComprado()) {
                Produto produto = item.produto();
                int disponivel = produto.isAtivo() ? produto.getEstoque() : 0;
                throw new EstoqueInsuficienteException(produto.getNome(), disponivel, item.quantidade());
            }
        }
        return carrinho;
    }

    private static List<ItemPedido> itensDoPedido(Carrinho carrinho) {
        return carrinho.getItens().stream().map(ItemPedido::de).toList();
    }

    private void baixarEstoque(Carrinho carrinho) {
        List<ItemCarrinho> emOrdem = carrinho.getItens().stream()
                .sorted(Comparator.comparing(ItemCarrinho::produtoId))
                .toList();
        for (ItemCarrinho item : emOrdem) {
            if (!produtoRepository.baixarEstoque(item.produtoId(), item.quantidade())) {
                int disponivel = produtoRepository.buscarPorId(item.produtoId()).map(Produto::getEstoque).orElse(0);
                throw new EstoqueInsuficienteException(item.produto().getNome(), disponivel, item.quantidade());
            }
        }
    }
}
