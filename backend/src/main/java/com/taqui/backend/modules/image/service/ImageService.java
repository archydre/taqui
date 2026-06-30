package com.taqui.backend.modules.image.service;

import com.taqui.backend.modules.image.exception.ImageProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.Base64;

@RequiredArgsConstructor
@Service
public class ImageService {

    private static final String RESIZE_QUEUE = "image.resize.request";

    private final RabbitTemplate rabbitTemplate;

    public byte[] resizeToThumbnail(byte[] imageBytes) {
        byte[] base64Request = Base64.getEncoder().encode(imageBytes);

        Message message = MessageBuilder
                .withBody(base64Request)
                .setContentType(MessageProperties.CONTENT_TYPE_BYTES)
                .build();

        Message reply;
        try {
            reply = rabbitTemplate.sendAndReceive(RESIZE_QUEUE, message);
        } catch (AmqpException ex) {
            throw new ImageProcessingException("Serviço de imagem indisponível");
        }
        if (reply == null) {
            throw new ImageProcessingException("Serviço de imagem não respondeu a tempo");
        }

        Object error = reply.getMessageProperties().getHeaders().get("error");
        if (error != null) {
            throw new ImageProcessingException("Não foi possível processar a imagem: " + error);
        }

        return Base64.getDecoder().decode(reply.getBody());
    }
}