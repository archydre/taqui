package com.taqui.backend.modules.auth.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taqui.backend.modules.auth.exception.EmailDeliveryException;
import com.taqui.backend.modules.auth.exception.InvalidVerificationTokenException;
import com.taqui.backend.modules.user.entity.User;
import com.taqui.backend.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private static final String EMAIL_QUEUE = "email.verify.request";

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;

    public void sendVerificationEmail(User user) {
        Message message;
        try {
            byte[] body = objectMapper.writeValueAsBytes(new VerificationEmailPayload(
                    user.getEmail(), user.getDisplayName(), user.getVerificationToken()));
            message = MessageBuilder
                    .withBody(body)
                    .setContentType(MessageProperties.CONTENT_TYPE_JSON)
                    .build();
        } catch (IOException ex) {
            throw new EmailDeliveryException("Falha ao montar o email de verificação");
        }

        Message reply;
        try {
            reply = rabbitTemplate.sendAndReceive(EMAIL_QUEUE, message);
        } catch (AmqpException ex) {
            throw new EmailDeliveryException("Serviço de email indisponível");
        }
        if (reply == null) {
            throw new EmailDeliveryException("Serviço de email não respondeu a tempo");
        }

        Object error = reply.getMessageProperties().getHeaders().get("error");
        if (error != null) {
            throw new EmailDeliveryException("Não foi possível enviar o email: " + error);
        }
    }

    @Transactional
    public void verifyEmail(String token) {
        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new InvalidVerificationTokenException("Token de verificação inválido"));

        if (user.getVerificationTokenExpiresAt() == null
                || user.getVerificationTokenExpiresAt().isBefore(Instant.now())) {
            throw new InvalidVerificationTokenException("Token de verificação expirado");
        }

        user.setEmailVerified(true);
        user.setVerificationToken(null);
        user.setVerificationTokenExpiresAt(null);
    }

    private record VerificationEmailPayload(String email, String nome, String token) {}
}
