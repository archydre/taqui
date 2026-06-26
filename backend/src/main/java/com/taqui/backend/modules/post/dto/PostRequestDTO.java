package com.taqui.backend.modules.post.dto;

import jakarta.validation.constraints.Size;

import java.util.UUID;

public record PostRequestDTO(
        @Size(max = 1000) String content,
        String imageUrl,
        UUID productId
) {}
