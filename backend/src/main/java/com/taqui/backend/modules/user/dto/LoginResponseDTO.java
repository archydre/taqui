package com.taqui.backend.modules.user.dto;

public record LoginResponseDTO(String token, String tokenType, long expiresInMinutes) {
}