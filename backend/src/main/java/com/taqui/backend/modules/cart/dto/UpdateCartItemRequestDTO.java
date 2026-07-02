package com.taqui.backend.modules.cart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateCartItemRequestDTO(
        @NotNull @Min(1) Integer quantity
) {}
