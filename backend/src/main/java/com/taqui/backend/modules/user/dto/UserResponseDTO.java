package com.taqui.backend.modules.user.dto;

import java.util.UUID;

public record UserResponseDTO(
        UUID userId,
        String email,
        String username,
        String displayName,
        String whatsapp,
        String pixKey) {
}
