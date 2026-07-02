package com.taqui.backend.modules.notification.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taqui.backend.modules.user.entity.User;
import com.taqui.backend.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

/**
 * Publica o email de notificação numa fila do RabbitMQ (fire-and-forget, sem RPC): o worker
 * Python consome e envia. Best-effort — qualquer falha aqui é logada e engolida, nunca derruba
 * o fluxo do pedido (o listener que chama isto já roda pós-commit, em thread separada).
 */
@Component
@RequiredArgsConstructor
public class NotificationEmailPublisher {

    private static final Logger log = LoggerFactory.getLogger(NotificationEmailPublisher.class);
    private static final String EMAIL_QUEUE = "notification.email.request";

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;

    public void enqueue(UUID recipientId, String subject, String body) {
        User user = userRepository.findById(recipientId).orElse(null);
        if (user == null || user.getEmail() == null || user.getEmail().isBlank()) {
            return;
        }
        try {
            byte[] payload = objectMapper.writeValueAsBytes(
                    new NotificationEmailPayload(user.getEmail(), user.getDisplayName(), subject, body));
            Message message = MessageBuilder
                    .withBody(payload)
                    .setContentType(MessageProperties.CONTENT_TYPE_JSON)
                    .build();
            rabbitTemplate.send(EMAIL_QUEUE, message);
        } catch (IOException ex) {
            log.warn("Falha ao montar o email de notificação para {}", recipientId, ex);
        }
    }

    private record NotificationEmailPayload(String email, String nome, String subject, String body) {}
}
