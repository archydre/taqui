package com.taqui.backend.modules.order.event;

import java.util.UUID;

/** Pedido enviado. Notifica o comprador (recipientId); actor = vendedor. */
public record OrderShippedEvent(
        UUID orderId,
        UUID recipientId,
        String actorName,
        String productName
) {}
