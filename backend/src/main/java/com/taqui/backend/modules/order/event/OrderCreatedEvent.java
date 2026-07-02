package com.taqui.backend.modules.order.event;

import java.util.UUID;

/** Pedido criado. Notifica o vendedor (recipientId); actor = comprador. */
public record OrderCreatedEvent(
        UUID orderId,
        UUID recipientId,
        String actorName,
        String productName
) {}
