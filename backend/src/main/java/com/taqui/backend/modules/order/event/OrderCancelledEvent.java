package com.taqui.backend.modules.order.event;

import java.util.UUID;

/** Pedido cancelado. Notifica a contraparte de quem cancelou (recipientId); actor = quem cancelou. */
public record OrderCancelledEvent(
        UUID orderId,
        UUID recipientId,
        String actorName,
        String productName
) {}
