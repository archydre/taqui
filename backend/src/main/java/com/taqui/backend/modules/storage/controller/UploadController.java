package com.taqui.backend.modules.storage.controller;

import com.taqui.backend.modules.image.service.ImageService;
import com.taqui.backend.modules.storage.dto.UploadResponseDTO;
import com.taqui.backend.modules.storage.exception.InvalidUploadException;
import com.taqui.backend.modules.storage.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Set;

@RestController
@RequestMapping("/uploads")
@RequiredArgsConstructor
public class UploadController {

    private static final Set<String> ALLOWED_TYPES =
            Set.of("image/png", "image/jpeg", "image/webp");

    private final StorageService storageService;
    private final ImageService imageService;

    @PostMapping
    public ResponseEntity<UploadResponseDTO> upload(
            @RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new InvalidUploadException("Arquivo vazio");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new InvalidUploadException("contentType deve ser image/png, image/jpeg ou image/webp");
        }

        byte[] original = file.getBytes();
        byte[] thumbnail = imageService.resizeToThumbnail(original);

        String imageUrl = storageService.putObject(original, contentType);
        String thumbnailUrl = storageService.putObject(thumbnail, "image/jpeg");

        return ResponseEntity.ok(new UploadResponseDTO(imageUrl, thumbnailUrl));
    }
}
