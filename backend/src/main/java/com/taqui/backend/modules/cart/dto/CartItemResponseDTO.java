package com.taqui.backend.modules.cart.dto;

import com.taqui.backend.modules.product.dto.ProductResponseDTO;

import java.time.Instant;
import java.util.UUID;

public record CartItemResponseDTO(
        UUID id,
        ProductResponseDTO product,
        Integer quantity,
        Instant createdAt,
        Instant updatedAt
) {}
