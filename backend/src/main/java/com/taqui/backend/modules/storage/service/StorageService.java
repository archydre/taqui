package com.taqui.backend.modules.storage.service;

public interface StorageService {

    String putObject(byte[] bytes, String contentType);
}
