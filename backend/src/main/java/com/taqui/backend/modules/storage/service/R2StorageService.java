package com.taqui.backend.modules.storage.service;

import com.taqui.backend.modules.storage.config.R2Properties;
import com.taqui.backend.modules.storage.dto.UploadResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class R2StorageService implements StorageService {

    private final S3Presigner s3Presigner;
    private final R2Properties r2Properties;

    @Override
    public UploadResponseDTO createUploadUrl(String contentType) {
        String extension = contentType.substring(contentType.indexOf('/') + 1);
        String key = UUID.randomUUID() + "." + extension;

        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(r2Properties.bucket())
                .key(key)
                .contentType(contentType)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(r2Properties.presignExpiryMinutes()))
                .putObjectRequest(objectRequest)
                .build();

        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);

        String uploadUrl = presignedRequest.url().toString();
        String publicUrl = r2Properties.publicBaseUrl() + "/" + key;

        return new UploadResponseDTO(uploadUrl, publicUrl);
    }
}
