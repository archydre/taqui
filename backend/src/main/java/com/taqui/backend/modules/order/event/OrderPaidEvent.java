package com.taqui.backend.modules.order.event;

import java.util.UUID;

/** Pagamento confirmado. Notifica o comprador (recipientId); actor = vendedor. */
public record OrderPaidEvent(
        UUID orderId,
        UUID recipientId,
        String actorName,
        String productName
) {}
