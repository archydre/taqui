package com.taqui.backend.modules.post.dto;

import com.taqui.backend.modules.product.dto.ProductResponseDTO;
import com.taqui.backend.modules.user.dto.UserPublicInfoDTO;

import java.time.Instant;
import java.util.UUID;

public record PostResponseDTO(
        UUID postId,
        String content,
        String imageUrl,
        ProductResponseDTO product,
        String type,
        UserPublicInfoDTO owner,
        Instant createdAt,
        Instant updatedAt
) {}
