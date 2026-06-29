package com.taqui.backend.modules.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderRequestDTO(
        @NotNull UUID productId,
        @NotNull @Min(1) Integer quantity,
        @NotBlank String freightService,
        @NotNull @PositiveOrZero BigDecimal freightPrice,
        @NotNull @Valid AddressDTO address
) {}
