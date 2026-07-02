package com.taqui.backend.modules.order.service;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class PixBrCodeGeneratorTest {

    @Test
    void crc16_bateOVetorCanonico() {
        // Check value oficial do CRC-16/CCITT-FALSE para "123456789".
        assertThat(PixBrCodeGenerator.crc16("123456789")).isEqualTo("29B1");
    }

    @Test
    void build_temAEstruturaEOsValores() {
        String p = PixBrCodeGenerator.build(
                "teste@taqui.com", new BigDecimal("25.00"), "Loja do Joao", "Mossoro", "abc123");

        assertThat(p).startsWith("000201");           // payload format
        assertThat(p).contains("0014br.gov.bcb.pix"); // GUI do Pix
        assertThat(p).contains("teste@taqui.com");    // chave
        assertThat(p).contains("5303986");            // moeda BRL
        assertThat(p).contains("540525.00");          // valor 25.00
        assertThat(p).contains("5802BR");             // país
    }

    @Test
    void build_oCrcNoFimBateComOPayload() {
        String p = PixBrCodeGenerator.build(
                "teste@taqui.com", new BigDecimal("10.50"), "Loja", "Cidade", "x1");
        String semCrc = p.substring(0, p.length() - 4);
        String crc = p.substring(p.length() - 4);

        assertThat(semCrc).endsWith("6304");
        assertThat(PixBrCodeGenerator.crc16(semCrc)).isEqualTo(crc);
    }

    @Test
    void build_tiraAcentosDoNomeECidade() {
        String p = PixBrCodeGenerator.build(
                "k", new BigDecimal("1.00"), "João Cção", "São Paulo", "*");
        assertThat(p).contains("Joao Ccao");
        assertThat(p).contains("Sao Paulo");
        assertThat(p).doesNotContain("ã");
    }
}
