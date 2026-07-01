package com.taqui.backend.modules.storage.service;

import com.taqui.backend.modules.storage.config.R2Properties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.util.UUID;

@Slf4j
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

    @Override
    public void deleteObject(String publicUrl) {
        if (publicUrl == null || publicUrl.isBlank()) {
            return;
        }
        // A URL é sempre publicBaseUrl + "/" + key, e a key não tem "/", então o pedaço
        // depois da última barra é a key.
        String key = publicUrl.substring(publicUrl.lastIndexOf('/') + 1);
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(r2Properties.bucket())
                    .key(key)
                    .build());
        } catch (Exception ex) {
            // Limpeza é best-effort: uma falha aqui não pode derrubar a exclusão no banco.
            log.warn("Falha ao apagar o objeto '{}' do R2: {}", key, ex.getMessage());
        }
    }
}
