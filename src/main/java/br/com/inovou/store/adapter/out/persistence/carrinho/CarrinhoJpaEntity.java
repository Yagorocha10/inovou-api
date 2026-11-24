package br.com.inovou.store.adapter.out.persistence.carrinho;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "carrinho")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CarrinhoJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false, unique = true)
    private Long usuarioId;

    @Column(name = "atualizado_em", nullable = false)
    private LocalDateTime atualizadoEm;

    @OneToMany(mappedBy = "carrinho", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<ItemCarrinhoJpaEntity> itens = new ArrayList<>();

    CarrinhoJpaEntity(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    @PrePersist
    @PreUpdate
    void aoSalvar() {
        atualizadoEm = LocalDateTime.now();
    }
}
