package br.com.inovou.store.adapter.out.persistence.pedido;

import br.com.inovou.store.application.consulta.Pagina;
import br.com.inovou.store.application.consulta.Paginacao;
import br.com.inovou.store.application.port.out.PedidoRepositoryPort;
import br.com.inovou.store.domain.exception.RecursoNaoEncontradoException;
import br.com.inovou.store.domain.pedido.EnderecoEntrega;
import br.com.inovou.store.domain.pedido.ItemPedido;
import br.com.inovou.store.domain.pedido.Pagamento;
import br.com.inovou.store.domain.pedido.Pedido;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@Transactional(readOnly = true)
public class PedidoPersistenceAdapter implements PedidoRepositoryPort {

    private static final Sort MAIS_RECENTES_PRIMEIRO = Sort.by(Sort.Direction.DESC, "criadoEm", "id");

    private final PedidoJpaRepository pedidoJpaRepository;

    public PedidoPersistenceAdapter(PedidoJpaRepository pedidoJpaRepository) {
        this.pedidoJpaRepository = pedidoJpaRepository;
    }

    @Override
    @Transactional
    public Pedido salvar(Pedido pedido) {
        PedidoJpaEntity entidade = pedido.getId() == null ? novaEntidade(pedido) : existente(pedido.getId());
        copiarEstado(pedido, entidade);
        PedidoJpaEntity salva = pedidoJpaRepository.saveAndFlush(entidade);
        if (salva.getCodigo() == null) {
            salva.setCodigo(gerarCodigo(salva.getId()));
            salva = pedidoJpaRepository.saveAndFlush(salva);
        }
        return paraDominio(salva);
    }

    @Override
    public Optional<Pedido> buscarPorId(Long id) {
        return pedidoJpaRepository.findById(id).map(PedidoPersistenceAdapter::paraDominio);
    }

    @Override
    public Pagina<Pedido> listarDoUsuario(Long usuarioId, Paginacao paginacao) {
        Page<PedidoJpaEntity> pagina = pedidoJpaRepository.findByUsuarioId(usuarioId,
                PageRequest.of(paginacao.pagina(), paginacao.tamanho(), MAIS_RECENTES_PRIMEIRO));
        return new Pagina<>(pagina.getContent().stream().map(PedidoPersistenceAdapter::paraDominio).toList(),
                paginacao.pagina(), paginacao.tamanho(), pagina.getTotalElements());
    }

    static String gerarCodigo(Long id) {
        return "INV-%06d".formatted(id);
    }

    private PedidoJpaEntity existente(Long id) {
        return pedidoJpaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pedido não encontrado"));
    }

    private static PedidoJpaEntity novaEntidade(Pedido pedido) {
        PedidoJpaEntity entidade = new PedidoJpaEntity();
        entidade.setUsuarioId(pedido.getUsuarioId());
        entidade.setCriadoEm(pedido.getCriadoEm());
        entidade.setFrete(pedido.getFrete());
        EnderecoEntrega endereco = pedido.getEndereco();
        entidade.setEntregaCep(endereco.cep());
        entidade.setEntregaLogradouro(endereco.logradouro());
        entidade.setEntregaNumero(endereco.numero());
        entidade.setEntregaComplemento(endereco.complemento());
        entidade.setEntregaBairro(endereco.bairro());
        entidade.setEntregaCidade(endereco.cidade());
        entidade.setEntregaUf(endereco.uf());
        for (ItemPedido item : pedido.getItens()) {
            entidade.getItens().add(new ItemPedidoJpaEntity(entidade, item.produtoId(), item.nome(), item.slug(),
                    item.imagem(), item.precoUnitario(), item.quantidade()));
        }
        return entidade;
    }

    private static void copiarEstado(Pedido pedido, PedidoJpaEntity entidade) {
        Pagamento pagamento = pedido.getPagamento();
        entidade.setStatus(pedido.getStatus());
        entidade.setFormaPagamento(pagamento.forma());
        entidade.setPagamentoAprovado(pagamento.aprovado());
        entidade.setInstrucoesPagamento(pagamento.instrucoes() == null ? "" : pagamento.instrucoes());
        entidade.setCodigoPix(pagamento.codigoPix());
    }

    private static Pedido paraDominio(PedidoJpaEntity entidade) {
        return new Pedido(
                entidade.getId(),
                entidade.getCodigo(),
                entidade.getUsuarioId(),
                entidade.getStatus(),
                entidade.getCriadoEm(),
                entidade.getItens().stream()
                        .map(item -> new ItemPedido(item.getProdutoId(), item.getNome(), item.getSlug(),
                                item.getImagem(), item.getPrecoUnitario(), item.getQuantidade()))
                        .toList(),
                entidade.getFrete(),
                new EnderecoEntrega(entidade.getEntregaCep(), entidade.getEntregaLogradouro(),
                        entidade.getEntregaNumero(), entidade.getEntregaComplemento(), entidade.getEntregaBairro(),
                        entidade.getEntregaCidade(), entidade.getEntregaUf()),
                new Pagamento(entidade.getFormaPagamento(), entidade.isPagamentoAprovado(),
                        entidade.getInstrucoesPagamento(), entidade.getCodigoPix()));
    }
}
