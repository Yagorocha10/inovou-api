package br.com.inovou.store.adapter.out.persistence.avaliacao;

import br.com.inovou.store.domain.avaliacao.Avaliacao;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "avaliacao")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AvaliacaoJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "produto_id", nullable = false)
    private Long produtoId;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(nullable = false, length = 80)
    private String autor;

    @Column(nullable = false)
    private int nota;

    @Column(length = 1000)
    private String comentario;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    AvaliacaoJpaEntity(Avaliacao avaliacao) {
        this.produtoId = avaliacao.produtoId();
        this.usuarioId = avaliacao.usuarioId();
        this.autor = avaliacao.autor();
        this.nota = avaliacao.nota();
        this.comentario = avaliacao.comentario();
        this.criadoEm = avaliacao.criadoEm();
    }
}
