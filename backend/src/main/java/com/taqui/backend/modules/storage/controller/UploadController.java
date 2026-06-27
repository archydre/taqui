package com.taqui.backend.modules.storage.controller;

import com.taqui.backend.modules.storage.dto.UploadRequestDTO;
import com.taqui.backend.modules.storage.dto.UploadResponseDTO;
import com.taqui.backend.modules.storage.service.StorageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/uploads")
@RequiredArgsConstructor
public class UploadController {

    private final StorageService storageService;

    @PostMapping
    public ResponseEntity<UploadResponseDTO> createUploadUrl(
            @Valid @RequestBody UploadRequestDTO uploadRequestDTO) {
        UploadResponseDTO response = storageService.createUploadUrl(uploadRequestDTO.contentType());
        return ResponseEntity.ok(response);
    }
}
