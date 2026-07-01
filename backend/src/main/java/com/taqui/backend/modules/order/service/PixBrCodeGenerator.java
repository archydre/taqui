package com.taqui.backend.modules.order.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;

/**
 * Gera o "Pix Copia e Cola" (BR Code — payload EMV do Banco Central) de um Pix estático com
 * valor. O front transforma essa string numa imagem de QR Code. Cada campo é um TLV:
 * id (2 dígitos) + tamanho do valor (2 dígitos) + valor.
 */
public final class PixBrCodeGenerator {

    private PixBrCodeGenerator() {}

    public static String build(String pixKey, BigDecimal amount, String merchantName,
                               String merchantCity, String txid) {
        String merchantAccount = tlv("00", "br.gov.bcb.pix") + tlv("01", pixKey);

        String payload =
                tlv("00", "01") +                                              // payload format
                tlv("26", merchantAccount) +                                   // merchant account (Pix)
                tlv("52", "0000") +                                            // merchant category code
                tlv("53", "986") +                                             // moeda: BRL
                tlv("54", amount.setScale(2, RoundingMode.HALF_UP).toPlainString()) + // valor
                tlv("58", "BR") +                                              // país
                tlv("59", sanitize(merchantName, 25)) +                        // nome do recebedor
                tlv("60", sanitize(merchantCity, 15)) +                        // cidade
                tlv("62", tlv("05", sanitizeTxid(txid))) +                     // txid (reference label)
                "6304";                                                        // id + tamanho do CRC

        return payload + crc16(payload);
    }

    private static String tlv(String id, String value) {
        return id + String.format("%02d", value.length()) + value;
    }

    // CRC16-CCITT (FALSE): polinômio 0x1021, inicial 0xFFFF, sem reflexão. Cobre o payload
    // INCLUINDO o "6304" do próprio campo do CRC.
    static String crc16(String payload) {
        int crc = 0xFFFF;
        for (byte b : payload.getBytes(StandardCharsets.US_ASCII)) {
            crc ^= (b & 0xFF) << 8;
            for (int i = 0; i < 8; i++) {
                crc = ((crc & 0x8000) != 0) ? (crc << 1) ^ 0x1021 : crc << 1;
                crc &= 0xFFFF;
            }
        }
        return String.format("%04X", crc);
    }

    // Tira acento/caracteres fora do ASCII imprimível e corta no tamanho máximo do campo.
    private static String sanitize(String value, int max) {
        String s = value == null ? "" : value;
        s = Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        s = s.replaceAll("[^\\x20-\\x7E]", "").trim();
        if (s.isEmpty()) s = "NA";
        return s.length() > max ? s.substring(0, max) : s;
    }

    // txid do Pix estático: alfanumérico, até 25 chars. "***" = sem txid específico.
    private static String sanitizeTxid(String txid) {
        String s = txid == null ? "" : txid.replaceAll("[^A-Za-z0-9]", "");
        if (s.isEmpty()) return "***";
        return s.length() > 25 ? s.substring(0, 25) : s;
    }
}
