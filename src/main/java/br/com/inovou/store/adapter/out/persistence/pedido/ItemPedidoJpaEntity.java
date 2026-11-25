package br.com.inovou.store.adapter.out.persistence.pedido;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "item_pedido")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ItemPedidoJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pedido_id")
    private PedidoJpaEntity pedido;

    @Column(name = "produto_id", nullable = false)
    private Long produtoId;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(nullable = false, length = 180)
    private String slug;

    @Column(length = 500)
    private String imagem;

    @Column(name = "preco_unitario", nullable = false, precision = 10, scale = 2)
    private BigDecimal precoUnitario;

    @Column(nullable = false)
    private int quantidade;

    ItemPedidoJpaEntity(PedidoJpaEntity pedido, Long produtoId, String nome, String slug, String imagem,
                        BigDecimal precoUnitario, int quantidade) {
        this.pedido = pedido;
        this.produtoId = produtoId;
        this.nome = nome;
        this.slug = slug;
        this.imagem = imagem;
        this.precoUnitario = precoUnitario;
        this.quantidade = quantidade;
    }
}
