package com.taqui.backend.modules.user.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateMeRequestDTO(
        @Pattern(regexp = "^\\d{12,13}$",
                message = "whatsapp: só dígitos com DDI+DDD, ex 5584999998888 (12 ou 13 dígitos)")
        String whatsapp,
        @Size(min = 1, max = 50) String displayName,
        @Size(min = 1, max = 77) String pixKey,
        @Pattern(regexp = "\\d{8}", message = "postalCode: CEP de origem com 8 dígitos") String postalCode
) {
}
