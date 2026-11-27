package br.com.inovou.store.application.service;

import br.com.inovou.store.application.consulta.AvaliacaoComProduto;
import br.com.inovou.store.application.consulta.AvaliacoesDoProduto;
import br.com.inovou.store.application.consulta.Paginacao;
import br.com.inovou.store.application.consulta.PermissaoAvaliacao;
import br.com.inovou.store.application.consulta.ResumoAvaliacoes;
import br.com.inovou.store.application.port.in.AvaliarProdutoUseCase;
import br.com.inovou.store.application.port.in.ConsultarAvaliacoesUseCase;
import br.com.inovou.store.application.port.out.AvaliacaoRepositoryPort;
import br.com.inovou.store.application.port.out.PedidoRepositoryPort;
import br.com.inovou.store.application.port.out.ProdutoRepositoryPort;
import br.com.inovou.store.application.port.out.UsuarioRepositoryPort;
import br.com.inovou.store.domain.avaliacao.Avaliacao;
import br.com.inovou.store.domain.exception.DomainException;
import br.com.inovou.store.domain.exception.RecursoNaoEncontradoException;
import br.com.inovou.store.domain.produto.Produto;
import br.com.inovou.store.domain.usuario.Usuario;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class AvaliacaoService implements AvaliarProdutoUseCase, ConsultarAvaliacoesUseCase {

    static final int LIMITE_MAXIMO_RECENTES = 12;

    private final AvaliacaoRepositoryPort avaliacaoRepository;
    private final ProdutoRepositoryPort produtoRepository;
    private final PedidoRepositoryPort pedidoRepository;
    private final UsuarioRepositoryPort usuarioRepository;
    private final Clock relogio;

    public AvaliacaoService(AvaliacaoRepositoryPort avaliacaoRepository, ProdutoRepositoryPort produtoRepository,
                            PedidoRepositoryPort pedidoRepository, UsuarioRepositoryPort usuarioRepository,
                            Clock relogio) {
        this.avaliacaoRepository = avaliacaoRepository;
        this.produtoRepository = produtoRepository;
        this.pedidoRepository = pedidoRepository;
        this.usuarioRepository = usuarioRepository;
        this.relogio = relogio;
    }

    @Override
    public Avaliacao avaliar(Long usuarioId, String slugProduto, int nota, String comentario) {
        Produto produto = produto(slugProduto);
        PermissaoAvaliacao permissao = permissaoPara(usuarioId, produto);
        if (!permissao.podeAvaliar()) {
            throw new DomainException(permissao.motivo());
        }
        Usuario usuario = usuarioRepository.buscarPorId(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente não encontrado"));
        return avaliacaoRepository.salvar(Avaliacao.nova(produto.getId(), usuarioId, usuario.getNome(), nota,
                comentario, LocalDateTime.now(relogio)));
    }

    @Override
    public PermissaoAvaliacao permissao(Long usuarioId, String slugProduto) {
        return permissaoPara(usuarioId, produto(slugProduto));
    }

    @Override
    public AvaliacoesDoProduto doProduto(String slugProduto, Paginacao paginacao) {
        Produto produto = produto(slugProduto);
        ResumoAvaliacoes resumo = avaliacaoRepository.resumosDosProdutos(List.of(produto.getId()))
                .getOrDefault(produto.getId(), ResumoAvaliacoes.vazio());
        return new AvaliacoesDoProduto(resumo, avaliacaoRepository.listarDoProduto(produto.getId(), paginacao));
    }

    @Override
    public Map<Long, ResumoAvaliacoes> resumosDosProdutos(Collection<Long> produtoIds) {
        if (produtoIds == null || produtoIds.isEmpty()) {
            return Map.of();
        }
        return avaliacaoRepository.resumosDosProdutos(produtoIds);
    }

    @Override
    public List<AvaliacaoComProduto> recentes(int limite) {
        return avaliacaoRepository.recentesComComentario(Math.clamp(limite, 1, LIMITE_MAXIMO_RECENTES));
    }

    private PermissaoAvaliacao permissaoPara(Long usuarioId, Produto produto) {
        if (usuarioId == null) {
            return PermissaoAvaliacao.negada("Entre na sua conta para avaliar este produto");
        }
        if (avaliacaoRepository.existe(produto.getId(), usuarioId)) {
            return PermissaoAvaliacao.negada("Você já avaliou este produto. Obrigado!");
        }
        if (!pedidoRepository.clienteRecebeuProduto(usuarioId, produto.getId())) {
            return PermissaoAvaliacao.negada("Você poderá avaliar este produto depois de recebê-lo");
        }
        return PermissaoAvaliacao.permitida();
    }

    private Produto produto(String slug) {
        return produtoRepository.buscarAtivoPorSlug(slug)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto não encontrado: " + slug));
    }
}
