package com.taqui.backend.modules.notification.listener;

import com.taqui.backend.modules.notification.entity.NotificationEntityType;
import com.taqui.backend.modules.notification.entity.NotificationType;
import com.taqui.backend.modules.notification.service.NotificationEmailPublisher;
import com.taqui.backend.modules.notification.service.NotificationMessages;
import com.taqui.backend.modules.notification.service.NotificationService;
import com.taqui.backend.modules.order.event.OrderCancelledEvent;
import com.taqui.backend.modules.order.event.OrderCreatedEvent;
import com.taqui.backend.modules.order.event.OrderPaidEvent;
import com.taqui.backend.modules.order.event.OrderShippedEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.UUID;

/**
 * Escuta os eventos de pedido e gera a notificação (in-app + email). Roda DEPOIS do commit
 * (AFTER_COMMIT — se o pedido deu rollback, não notifica) e em thread separada (@Async — não
 * trava a request). É best-effort: falha na notificação nunca afeta a ação principal.
 */
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private static final Logger log = LoggerFactory.getLogger(NotificationEventListener.class);

    private final NotificationService notificationService;
    private final NotificationEmailPublisher emailPublisher;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderCreated(OrderCreatedEvent event) {
        handle(NotificationType.NEW_ORDER, event.recipientId(), event.orderId(),
                event.actorName(), event.productName(), true);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderPaid(OrderPaidEvent event) {
        handle(NotificationType.PAYMENT_CONFIRMED, event.recipientId(), event.orderId(),
                event.actorName(), event.productName(), true);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderShipped(OrderShippedEvent event) {
        handle(NotificationType.ORDER_SHIPPED, event.recipientId(), event.orderId(),
                event.actorName(), event.productName(), true);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderCancelled(OrderCancelledEvent event) {
        handle(NotificationType.ORDER_CANCELLED, event.recipientId(), event.orderId(),
                event.actorName(), event.productName(), false);
    }

    private void handle(NotificationType type, UUID recipientId, UUID orderId,
                        String actorName, String productName, boolean email) {
        String text = NotificationMessages.text(type, actorName, productName);
        try {
            notificationService.notify(recipientId, type, NotificationEntityType.ORDER, orderId, text);
        } catch (Exception ex) {
            log.warn("Falha ao gravar notificação in-app ({}) para {}", type, recipientId, ex);
        }
        if (email) {
            try {
                emailPublisher.enqueue(recipientId, NotificationMessages.subject(type), text);
            } catch (Exception ex) {
                log.warn("Falha ao publicar email de notificação ({}) para {}", type, recipientId, ex);
            }
        }
    }
}
