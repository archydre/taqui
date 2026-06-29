package com.taqui.backend.modules.freight.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.List;

public record FreightQuoteRequestDTO(
        @NotBlank @Pattern(regexp = "\\d{8}", message = "CEP deve ter 8 dígitos") String fromPostalCode,
        @NotBlank @Pattern(regexp = "\\d{8}", message = "CEP deve ter 8 dígitos") String toPostalCode,
        @NotEmpty @Valid List<Item> items
) {
    public record Item(
            @NotNull @Positive Integer width,
            @NotNull @Positive Integer height,
            @NotNull @Positive Integer length,
            @NotNull @Positive Double weight,
            @PositiveOrZero Double insuranceValue,
            @Positive Integer quantity
    ) {}
}