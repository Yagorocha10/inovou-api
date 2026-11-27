package br.com.inovou.store.adapter.out.pagamento;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.Locale;

final class PixCopiaECola {

    static final int TAMANHO_MAXIMO_NOME = 25;
    static final int TAMANHO_MAXIMO_CIDADE = 15;
    static final int TAMANHO_MAXIMO_IDENTIFICADOR = 25;

    private PixCopiaECola() {
    }

    static String gerar(String chave, String nomeRecebedor, String cidade, BigDecimal valor, String identificador) {
        String contaRecebedor = campo("00", "br.gov.bcb.pix") + campo("01", chave.strip());
        String dadosAdicionais = campo("05", limparIdentificador(identificador));
        String semCrc = campo("00", "01")
                + campo("26", contaRecebedor)
                + campo("52", "0000")
                + campo("53", "986")
                + campo("54", valor.setScale(2, RoundingMode.HALF_UP).toPlainString())
                + campo("58", "BR")
                + campo("59", limparTexto(nomeRecebedor, TAMANHO_MAXIMO_NOME))
                + campo("60", limparTexto(cidade, TAMANHO_MAXIMO_CIDADE))
                + campo("62", dadosAdicionais)
                + "6304";
        return semCrc + crc16(semCrc);
    }

    static String crc16(String texto) {
        int crc = 0xFFFF;
        for (byte b : texto.getBytes(StandardCharsets.UTF_8)) {
            crc ^= (b & 0xFF) << 8;
            for (int bit = 0; bit < 8; bit++) {
                crc = (crc & 0x8000) != 0 ? (crc << 1) ^ 0x1021 : crc << 1;
                crc &= 0xFFFF;
            }
        }
        return "%04X".formatted(crc);
    }

    private static String campo(String id, String valor) {
        if (valor.length() > 99) {
            throw new IllegalArgumentException("O campo %s do Pix passou de 99 caracteres".formatted(id));
        }
        return id + "%02d".formatted(valor.length()) + valor;
    }

    static String limparTexto(String texto, int tamanhoMaximo) {
        String semAcento = Normalizer.normalize(texto == null ? "" : texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toUpperCase(Locale.ROOT)
                .replaceAll("[^A-Z0-9 ]", "")
                .replaceAll("\\s+", " ")
                .strip();
        return semAcento.length() > tamanhoMaximo ? semAcento.substring(0, tamanhoMaximo).strip() : semAcento;
    }

    private static String limparIdentificador(String identificador) {
        String limpo = identificador == null ? "" : identificador.replaceAll("[^A-Za-z0-9]", "");
        if (limpo.isEmpty()) {
            return "***";
        }
        return limpo.length() > TAMANHO_MAXIMO_IDENTIFICADOR ? limpo.substring(0, TAMANHO_MAXIMO_IDENTIFICADOR) : limpo;
    }
}
