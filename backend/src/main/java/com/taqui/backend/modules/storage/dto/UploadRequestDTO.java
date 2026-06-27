package com.taqui.backend.modules.storage.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UploadRequestDTO(
        @NotBlank
        @Pattern(
                regexp = "image/(png|jpeg|webp)",
                message = "contentType deve ser image/png, image/jpeg ou image/webp"
        )
        String contentType
) {}
