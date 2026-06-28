package com.taqui.backend.modules.freight.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taqui.backend.modules.freight.dto.FreightOptionDTO;
import com.taqui.backend.modules.freight.dto.FreightQuoteRequestDTO;
import com.taqui.backend.modules.freight.exception.FreightUnavailableException;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.stream.IntStream;

@RequiredArgsConstructor
@Service
public class FreightService {

    private static final String FREIGHT_QUEUE = "freight.calculate.request";

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    public List<FreightOptionDTO> quote(FreightQuoteRequestDTO request) {
        Message reply = call(toWorkerRequest(request));

        Object error = reply.getMessageProperties().getHeaders().get("error");
        if (error != null) {
            throw new FreightUnavailableException("Não foi possível calcular o frete: " + error);
        }
        try {
            return objectMapper.readValue(reply.getBody(), new TypeReference<List<FreightOptionDTO>>() {});
        } catch (IOException ex) {
            throw new FreightUnavailableException("Resposta inválida do serviço de frete");
        }
    }

    private Message call(WorkerRequest workerRequest) {
        Message message;
        try {
            message = MessageBuilder
                    .withBody(objectMapper.writeValueAsBytes(workerRequest))
                    .setContentType(MessageProperties.CONTENT_TYPE_JSON)
                    .build();
        } catch (IOException ex) {
            throw new FreightUnavailableException("Falha ao montar a requisição de frete");
        }

        Message reply;
        try {
            reply = rabbitTemplate.sendAndReceive(FREIGHT_QUEUE, message);
        } catch (AmqpException ex) {
            throw new FreightUnavailableException("Serviço de frete indisponível");
        }
        if (reply == null) {
            throw new FreightUnavailableException("Serviço de frete não respondeu a tempo");
        }
        return reply;
    }

    private WorkerRequest toWorkerRequest(FreightQuoteRequestDTO request) {
        List<FreightQuoteRequestDTO.Item> items = request.items();
        List<WorkerProduct> produtos = IntStream.range(0, items.size())
                .mapToObj(i -> {
                    FreightQuoteRequestDTO.Item item = items.get(i);
                    return new WorkerProduct(
                            "item-" + (i + 1),
                            item.width(),
                            item.height(),
                            item.length(),
                            item.weight(),
                            item.insuranceValue() != null ? item.insuranceValue() : 0.0,
                            item.quantity() != null ? item.quantity() : 1);
                })
                .toList();

        return new WorkerRequest(new Cep(request.fromPostalCode()), new Cep(request.toPostalCode()), produtos);
    }

    private record WorkerRequest(Cep remetente, Cep destinatario, List<WorkerProduct> produtos) {}

    private record Cep(@JsonProperty("postal_code") String postalCode) {}

    private record WorkerProduct(
            String id, int width, int height, int length, double weight,
            @JsonProperty("insurance_value") double insuranceValue, int quantity) {}
}
