package com.taqui.backend.modules.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequestDTO(
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8, max = 72) String password,
        @NotBlank
        @Pattern(regexp = "^[a-z0-9._]{3,30}$",
                message = "username: 3 a 30 caracteres, só minúsculas, números, ponto e underscore")
        String username,
        @NotBlank @Size(min = 1, max = 50) String displayName
) {
}
