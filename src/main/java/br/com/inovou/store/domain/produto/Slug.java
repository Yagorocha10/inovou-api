package br.com.inovou.store.domain.produto;

import br.com.inovou.store.domain.exception.DomainException;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

public final class Slug {

    private static final Pattern MARCAS_DE_ACENTO = Pattern.compile("\\p{M}");
    private static final Pattern NAO_ALFANUMERICO = Pattern.compile("[^a-z0-9]+");
    private static final Pattern HIFENS_NAS_PONTAS = Pattern.compile("^-+|-+$");

    private Slug() {
    }

    public static String de(String texto) {
        if (texto == null || texto.isBlank()) {
            throw new DomainException("Não é possível gerar o slug de um texto vazio");
        }
        String semAcentos = MARCAS_DE_ACENTO
                .matcher(Normalizer.normalize(texto, Normalizer.Form.NFD))
                .replaceAll("");
        String comHifens = NAO_ALFANUMERICO
                .matcher(semAcentos.toLowerCase(Locale.ROOT))
                .replaceAll("-");
        String slug = HIFENS_NAS_PONTAS.matcher(comHifens).replaceAll("");
        if (slug.isEmpty()) {
            throw new DomainException("O texto \"%s\" não gera um slug válido".formatted(texto));
        }
        return slug;
    }
}
