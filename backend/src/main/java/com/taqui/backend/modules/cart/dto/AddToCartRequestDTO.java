package com.taqui.backend.modules.cart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AddToCartRequestDTO(
        @NotNull UUID productId,
        @NotNull @Min(1) Integer quantity
) {}
