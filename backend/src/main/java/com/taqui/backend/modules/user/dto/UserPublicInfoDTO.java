package com.taqui.backend.modules.user.dto;

import java.util.UUID;

public record UserPublicInfoDTO(UUID userId, String username) {
}
