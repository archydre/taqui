package com.taqui.backend.modules.notification.dto;

import com.taqui.backend.modules.notification.entity.Notification;
import com.taqui.backend.modules.notification.entity.NotificationEntityType;
import com.taqui.backend.modules.notification.entity.NotificationType;

import java.time.Instant;
import java.util.UUID;

public record NotificationResponseDTO(
        UUID id,
        NotificationType type,
        NotificationEntityType relatedEntityType,
        UUID relatedEntityId,
        String message,
        boolean read,
        Instant createdAt
) {
    public static NotificationResponseDTO from(Notification n) {
        return new NotificationResponseDTO(
                n.getId(), n.getType(), n.getRelatedEntityType(), n.getRelatedEntityId(),
                n.getMessage(), n.isRead(), n.getCreatedAt());
    }
}
