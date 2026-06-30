package com.taqui.backend.modules.comment.dto;

import com.taqui.backend.modules.user.dto.UserPublicInfoDTO;

import java.time.Instant;
import java.util.UUID;

public record CommentResponseDTO(
        UUID commentId,
        UserPublicInfoDTO author,
        String content,
        Instant createdAt
) {}
