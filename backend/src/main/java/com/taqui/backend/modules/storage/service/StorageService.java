package com.taqui.backend.modules.storage.service;

public interface StorageService {

    String putObject(byte[] bytes, String contentType);

    /** Apaga um objeto pela URL pública. No-op se a URL for nula/vazia; best-effort (não lança). */
    void deleteObject(String publicUrl);
}
