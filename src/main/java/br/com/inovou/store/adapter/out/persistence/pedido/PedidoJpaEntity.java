package br.com.inovou.store.adapter.out.persistence.pedido;

import br.com.inovou.store.domain.pedido.FormaPagamento;
import br.com.inovou.store.domain.pedido.StatusPedido;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pedido")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PedidoJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, length = 20)
    private String codigo;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatusPedido status;

    @Enumerated(EnumType.STRING)
    @Column(name = "forma_pagamento", nullable = false, length = 20)
    private FormaPagamento formaPagamento;

    @Column(name = "pagamento_aprovado", nullable = false)
    private boolean pagamentoAprovado;

    @Column(name = "instrucoes_pagamento", nullable = false, length = 300)
    private String instrucoesPagamento;

    @Column(name = "codigo_pix", length = 300)
    private String codigoPix;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal frete;

    @Column(name = "entrega_cep", nullable = false, length = 9)
    private String entregaCep;

    @Column(name = "entrega_logradouro", nullable = false, length = 150)
    private String entregaLogradouro;

    @Column(name = "entrega_numero", nullable = false, length = 20)
    private String entregaNumero;

    @Column(name = "entrega_complemento", length = 80)
    private String entregaComplemento;

    @Column(name = "entrega_bairro", nullable = false, length = 80)
    private String entregaBairro;

    @Column(name = "entrega_cidade", nullable = false, length = 80)
    private String entregaCidade;

    @Column(name = "entrega_uf", nullable = false, length = 2)
    private String entregaUf;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private LocalDateTime atualizadoEm;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<ItemPedidoJpaEntity> itens = new ArrayList<>();

    @PrePersist
    @PreUpdate
    void aoSalvar() {
        atualizadoEm = LocalDateTime.now();
    }
}
