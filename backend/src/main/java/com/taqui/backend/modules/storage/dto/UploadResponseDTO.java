package com.taqui.backend.modules.storage.dto;

public record UploadResponseDTO(
        String uploadUrl,
        String publicUrl
) {}
