package br.com.inovou.store.adapter.in.web.avaliacao;

import br.com.inovou.store.domain.avaliacao.Avaliacao;

import java.time.LocalDateTime;

public record AvaliacaoResponse(Long id, int nota, String comentario, String autor, LocalDateTime criadoEm) {

    static AvaliacaoResponse de(Avaliacao avaliacao) {
        return new AvaliacaoResponse(avaliacao.id(), avaliacao.nota(), avaliacao.comentario(), avaliacao.autor(),
                avaliacao.criadoEm());
    }
}
