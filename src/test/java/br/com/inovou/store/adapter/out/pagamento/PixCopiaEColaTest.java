package br.com.inovou.store.adapter.out.pagamento;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class PixCopiaEColaTest {

    @Test
    void crcBateComOExemploDoManualDoBancoCentral() {
        String exemplo = "00020126580014br.gov.bcb.pix0136123e4567-e12b-12d1-a456-426655440000"
                + "5204000053039865802BR5913Fulano de Tal6008BRASILIA62070503***6304";

        assertThat(PixCopiaECola.crc16(exemplo)).isEqualTo("1D3D");
        assertThat(PixCopiaECola.crc16("123456789")).isEqualTo("29B1");
    }

    @Test
    void montaOCodigoComChaveValorNomeCidadeEPedido() {
        String codigo = PixCopiaECola.gerar("chave-teste@inovou.local", "Yago da Silva Rocha", "Belém",
                new BigDecimal("90"), "INV-000004");

        assertThat(codigo).isEqualTo("00020126460014br.gov.bcb.pix0124chave-teste@inovou.local"
                + "520400005303986540590.005802BR5919YAGO DA SILVA ROCHA6005BELEM"
                + "62130509INV00000463048C21");
        assertThat(codigo).endsWith(PixCopiaECola.crc16(codigo.substring(0, codigo.length() - 4)));
    }

    @Test
    void cortaNomeECidadeNoLimiteETiraAcentos() {
        assertThat(PixCopiaECola.limparTexto("Loja de Eletrônicos Inovou Store Ltda", 25))
                .isEqualTo("LOJA DE ELETRONICOS INOVO");
        assertThat(PixCopiaECola.limparTexto("Santa Bárbara do Pará", 15)).isEqualTo("SANTA BARBARA D");
    }
}
