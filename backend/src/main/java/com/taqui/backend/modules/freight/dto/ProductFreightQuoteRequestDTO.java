package com.taqui.backend.modules.freight.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

public record ProductFreightQuoteRequestDTO(
        @NotBlank @Pattern(regexp = "\\d{8}", message = "CEP deve ter 8 dígitos") String toPostalCode,
        @Positive Integer quantity
) {}
