package com.taqui.backend.modules.product.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record ProductRequestDTO(
        @NotBlank @Size(max = 100) String productName,
        @Size(max = 500) String productDescription,
        @NotNull @Positive @Digits(integer = 10, fraction = 2) BigDecimal price,
        String imageUrl,
        String thumbnailUrl,
        @Positive BigDecimal weight,
        @Positive BigDecimal width,
        @Positive BigDecimal height,
        @Positive BigDecimal length) {}
