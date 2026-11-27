package br.com.inovou.store.domain.pedido;

import br.com.inovou.store.domain.exception.DomainException;
import br.com.inovou.store.domain.exception.TransicaoInvalidaException;
import br.com.inovou.store.domain.frete.Frete;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public class Pedido {

    private final Long id;
    private final String codigo;
    private final Long usuarioId;
    private StatusPedido status;
    private final LocalDateTime criadoEm;
    private final List<ItemPedido> itens;
    private final BigDecimal frete;
    private final BigDecimal desconto;
    private final EnderecoEntrega endereco;
    private Pagamento pagamento;

    public Pedido(Long id, String codigo, Long usuarioId, StatusPedido status, LocalDateTime criadoEm,
                  List<ItemPedido> itens, BigDecimal frete, EnderecoEntrega endereco, Pagamento pagamento) {
        this(id, codigo, usuarioId, status, criadoEm, itens, frete, BigDecimal.ZERO, endereco, pagamento);
    }

    public Pedido(Long id, String codigo, Long usuarioId, StatusPedido status, LocalDateTime criadoEm,
                  List<ItemPedido> itens, BigDecimal frete, BigDecimal desconto, EnderecoEntrega endereco,
                  Pagamento pagamento) {
        if (usuarioId == null) {
            throw new DomainException("O pedido precisa pertencer a um usuário");
        }
        if (itens == null || itens.isEmpty()) {
            throw new DomainException("O pedido precisa ter pelo menos um item");
        }
        if (endereco == null) {
            throw new DomainException("O pedido precisa de um endereço de entrega");
        }
        if (pagamento == null) {
            throw new DomainException("O pedido precisa de uma forma de pagamento");
        }
        this.id = id;
        this.codigo = codigo;
        this.usuarioId = usuarioId;
        this.status = status;
        this.criadoEm = criadoEm;
        this.itens = List.copyOf(itens);
        this.frete = frete == null ? BigDecimal.ZERO : frete;
        this.desconto = desconto == null ? BigDecimal.ZERO : desconto;
        if (this.desconto.signum() < 0 || this.desconto.compareTo(subtotal()) > 0) {
            throw new DomainException("O desconto do pedido deve ficar entre zero e o valor dos produtos");
        }
        this.endereco = endereco;
        this.pagamento = pagamento;
    }

    public static Pedido novo(Long usuarioId, List<ItemPedido> itens, EnderecoEntrega endereco, Frete frete,
                              FormaPagamento forma, LocalDateTime agora) {
        if (forma == null) {
            throw new DomainException("Escolha uma forma de pagamento");
        }
        BigDecimal desconto = forma == FormaPagamento.PIX ? DescontoPix.sobre(somar(itens)) : BigDecimal.ZERO;
        return new Pedido(null, null, usuarioId, StatusPedido.AGUARDANDO_PAGAMENTO, agora, itens,
                frete.valor(), desconto, endereco, Pagamento.pendente(forma));
    }

    private static BigDecimal somar(List<ItemPedido> itens) {
        if (itens == null) {
            return BigDecimal.ZERO;
        }
        return itens.stream().map(ItemPedido::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void registrarPagamento(Pagamento resultado) {
        if (resultado.forma() != pagamento.forma()) {
            throw new DomainException("O pagamento recebido não corresponde à forma escolhida");
        }
        pagamento = resultado;
        if (resultado.aprovado() && status == StatusPedido.AGUARDANDO_PAGAMENTO) {
            avancarPara(StatusPedido.PAGO);
        }
    }

    public void avancarPara(StatusPedido destino) {
        if (!status.podeIrPara(destino)) {
            throw new TransicaoInvalidaException("O pedido %s não pode passar de \"%s\" para \"%s\""
                    .formatted(codigoOuId(), status.getDescricao(), destino.getDescricao()));
        }
        status = destino;
    }

    public void cancelarPeloCliente() {
        if (!podeSerCanceladoPeloCliente()) {
            throw new TransicaoInvalidaException("O pedido %s está \"%s\" e não pode mais ser cancelado"
                    .formatted(codigoOuId(), status.getDescricao()));
        }
        status = StatusPedido.CANCELADO;
    }

    public Set<StatusPedido> proximosStatus() {
        return status.proximosPermitidos();
    }

    public boolean podeSerCanceladoPeloCliente() {
        return status.clientePodeCancelar();
    }

    public boolean pertenceA(Long idUsuario) {
        return usuarioId.equals(idUsuario);
    }

    public BigDecimal subtotal() {
        return somar(itens);
    }

    public BigDecimal total() {
        return subtotal().add(frete).subtract(desconto);
    }

    public int quantidadeItens() {
        return itens.stream().mapToInt(ItemPedido::quantidade).sum();
    }

    private String codigoOuId() {
        return codigo != null ? codigo : String.valueOf(id);
    }

    public Long getId() {
        return id;
    }

    public String getCodigo() {
        return codigo;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public StatusPedido getStatus() {
        return status;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public List<ItemPedido> getItens() {
        return itens;
    }

    public BigDecimal getFrete() {
        return frete;
    }

    public BigDecimal getDesconto() {
        return desconto;
    }

    public EnderecoEntrega getEndereco() {
        return endereco;
    }

    public Pagamento getPagamento() {
        return pagamento;
    }
}
