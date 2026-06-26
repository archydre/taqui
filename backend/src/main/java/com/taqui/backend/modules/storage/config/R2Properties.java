package com.taqui.backend.modules.storage.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "r2")
public record R2Properties(
        String endpoint,
        String accessKeyId,
        String secretAccessKey,
        String bucket,
        String region,
        String publicBaseUrl,
        long presignExpiryMinutes
) {
}
