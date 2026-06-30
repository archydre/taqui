package com.taqui.backend.modules.storage.service;

import com.taqui.backend.modules.storage.config.R2Properties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class R2StorageService implements StorageService {

    private final R2Properties r2Properties;
    private final S3Client s3Client;

    @Override
    public String putObject(byte[] bytes, String contentType) {
        String extension = contentType.substring(contentType.indexOf('/') + 1);
        String key = UUID.randomUUID() + "." + extension;

        PutObjectRequest req = PutObjectRequest.builder()
                .bucket(r2Properties.bucket())
                .key(key)
                .contentType(contentType)
                .build();

        s3Client.putObject(req, RequestBody.fromBytes(bytes));
        return r2Properties.publicBaseUrl() + "/" + key;
    }
}
