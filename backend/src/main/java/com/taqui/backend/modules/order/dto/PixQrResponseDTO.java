package com.taqui.backend.modules.order.dto;

import java.math.BigDecimal;

/** O "copia e cola" do Pix (BR Code) + o valor; o front gera a imagem do QR a partir do copyPaste. */
public record PixQrResponseDTO(String copyPaste, BigDecimal amount) {}
