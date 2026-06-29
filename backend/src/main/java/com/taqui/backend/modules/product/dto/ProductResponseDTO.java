package com.taqui.backend.modules.product.dto;

import com.taqui.backend.modules.user.dto.UserPublicInfoDTO;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ProductResponseDTO(
        UUID productId,
        String productName,
        String productDescription,
        BigDecimal price,
        String imageUrl,
        BigDecimal weight,
        BigDecimal width,
        BigDecimal height,
        BigDecimal length,
        UserPublicInfoDTO owner,
        Instant createdAt,
        Instant updatedAt
) {}
