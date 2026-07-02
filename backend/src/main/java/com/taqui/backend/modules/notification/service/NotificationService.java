package com.taqui.backend.modules.notification.service;

import com.taqui.backend.modules.notification.entity.Notification;
import com.taqui.backend.modules.notification.entity.NotificationEntityType;
import com.taqui.backend.modules.notification.entity.NotificationType;
import com.taqui.backend.modules.notification.exception.NotificationNotFoundException;
import com.taqui.backend.modules.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    // REQUIRES_NEW: a notificação é uma unidade própria, desacoplada da transação do pedido
    // (que já commitou quando o listener AFTER_COMMIT roda). Sem isto, com executor síncrono
    // o insert entraria na transação morta e se perderia.
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Notification notify(UUID recipientId, NotificationType type,
                               NotificationEntityType entityType, UUID entityId, String message) {
        Notification notification = new Notification();
        notification.setRecipientId(recipientId);
        notification.setType(type);
        notification.setRelatedEntityType(entityType);
        notification.setRelatedEntityId(entityId);
        notification.setMessage(message);
        return notificationRepository.save(notification);
    }

    @Transactional(readOnly = true)
    public Page<Notification> findMine(UUID recipientId, Pageable pageable) {
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(recipientId, pageable);
    }

    @Transactional(readOnly = true)
    public long unreadCount(UUID recipientId) {
        return notificationRepository.countByRecipientIdAndReadFalse(recipientId);
    }

    @Transactional
    public void markRead(UUID id, UUID recipientId) {
        Notification notification = notificationRepository.findByIdAndRecipientId(id, recipientId)
                .orElseThrow(() -> new NotificationNotFoundException("Notificação não encontrada: " + id));
        notification.setRead(true);
    }

    @Transactional
    public void markAllRead(UUID recipientId) {
        notificationRepository.markAllRead(recipientId);
    }
}
