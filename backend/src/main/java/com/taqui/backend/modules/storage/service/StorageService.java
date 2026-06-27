package com.taqui.backend.modules.storage.service;

import com.taqui.backend.modules.storage.dto.UploadResponseDTO;

public interface StorageService {

    UploadResponseDTO createUploadUrl(String contentType);
}
