package com.taqui.backend.modules.notification.service;

import com.taqui.backend.modules.notification.entity.NotificationType;

/** Monta o texto da notificação (usado tanto no in-app quanto no corpo do email). */
public final class NotificationMessages {

    private NotificationMessages() {}

    public static String text(NotificationType type, String actorName, String productName) {
        return switch (type) {
            case NEW_ORDER -> "%s fez um pedido de %s".formatted(actorName, productName);
            case PAYMENT_CONFIRMED -> "%s confirmou o pagamento do seu pedido de %s".formatted(actorName, productName);
            case ORDER_SHIPPED -> "%s enviou seu pedido de %s".formatted(actorName, productName);
            case ORDER_CANCELLED -> "%s cancelou o pedido de %s".formatted(actorName, productName);
        };
    }

    public static String subject(NotificationType type) {
        return switch (type) {
            case NEW_ORDER -> "Você recebeu um novo pedido no taqui";
            case PAYMENT_CONFIRMED -> "Pagamento confirmado no taqui";
            case ORDER_SHIPPED -> "Seu pedido foi enviado";
            case ORDER_CANCELLED -> "Pedido cancelado no taqui";
        };
    }
}
