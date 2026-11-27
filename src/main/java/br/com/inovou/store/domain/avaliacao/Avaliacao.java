package br.com.inovou.store.domain.avaliacao;

import br.com.inovou.store.domain.exception.DomainException;

import java.time.LocalDateTime;

public record Avaliacao(Long id, Long produtoId, Long usuarioId, String autor, int nota, String comentario,
                        LocalDateTime criadoEm) {

    public static final int NOTA_MINIMA = 1;
    public static final int NOTA_MAXIMA = 5;
    public static final int TAMANHO_MAXIMO_COMENTARIO = 1000;
    private static final int TAMANHO_MAXIMO_PRIMEIRO_NOME = 60;

    public Avaliacao {
        if (produtoId == null) {
            throw new DomainException("A avaliação precisa de um produto");
        }
        if (usuarioId == null) {
            throw new DomainException("A avaliação precisa de um cliente");
        }
        if (nota < NOTA_MINIMA || nota > NOTA_MAXIMA) {
            throw new DomainException("A nota deve ser de %d a %d estrelas".formatted(NOTA_MINIMA, NOTA_MAXIMA));
        }
        if (autor == null || autor.isBlank()) {
            throw new DomainException("A avaliação precisa do nome de quem avaliou");
        }
        autor = autor.strip();
        comentario = (comentario == null || comentario.isBlank()) ? null : comentario.strip();
        if (comentario != null && comentario.length() > TAMANHO_MAXIMO_COMENTARIO) {
            throw new DomainException("O comentário pode ter no máximo %d caracteres"
                    .formatted(TAMANHO_MAXIMO_COMENTARIO));
        }
    }

    public static Avaliacao nova(Long produtoId, Long usuarioId, String nomeDoCliente, int nota, String comentario,
                                 LocalDateTime agora) {
        return new Avaliacao(null, produtoId, usuarioId, nomePublico(nomeDoCliente), nota, comentario, agora);
    }

    public static String nomePublico(String nomeCompleto) {
        if (nomeCompleto == null || nomeCompleto.isBlank()) {
            throw new DomainException("A avaliação precisa do nome de quem avaliou");
        }
        String[] partes = nomeCompleto.strip().split("\\s+");
        String primeiro = partes[0].length() > TAMANHO_MAXIMO_PRIMEIRO_NOME
                ? partes[0].substring(0, TAMANHO_MAXIMO_PRIMEIRO_NOME)
                : partes[0];
        if (partes.length == 1) {
            return primeiro;
        }
        String ultimo = partes[partes.length - 1];
        return primeiro + " " + Character.toUpperCase(ultimo.charAt(0)) + ".";
    }
}
