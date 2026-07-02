package com.taqui.backend;

import com.taqui.backend.modules.notification.entity.Notification;
import com.taqui.backend.modules.notification.entity.NotificationEntityType;
import com.taqui.backend.modules.notification.entity.NotificationType;
import com.taqui.backend.modules.notification.repository.NotificationRepository;
import com.taqui.backend.modules.product.entity.Product;
import com.taqui.backend.modules.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class NotificationIT extends AbstractIntegrationTest {

    /**
     * Nos testes o @Async roda SÍNCRONO: assim o @TransactionalEventListener(AFTER_COMMIT)
     * executa inline (após o commit da request) e a notificação já está gravada quando o
     * mockMvc.perform(...) retorna — sem corrida entre o assert e a thread do pool.
     */
    @TestConfiguration
    static class SyncAsyncConfig {
        @Bean
        TaskExecutor taskExecutor() {
            return new SyncTaskExecutor();
        }
    }

    @Autowired
    private NotificationRepository notificationRepository;

    @BeforeEach
    void clearNotifications() {
        notificationRepository.deleteAll();
    }

    // ---- eventos de pedido geram notificação pra contraparte certa ----

    @Test
    void criarPedido_notificaVendedor_comMensagemELink() throws Exception {
        User vendedor = givenUser("vendedor", "5584999998888");
        User comprador = givenUser("comprador", "5584999990000");
        Product product = givenProduct(vendedor, "Caneca Azul", "ceramica");

        UUID orderId = criaPedido(comprador, product.getProductId());

        List<Notification> doVendedor = notificationRepository
                .findByRecipientIdOrderByCreatedAtDesc(vendedor.getUserId(), org.springframework.data.domain.Pageable.unpaged())
                .getContent();
        assertEquals(1, doVendedor.size());
        Notification n = doVendedor.get(0);
        assertEquals(NotificationType.NEW_ORDER, n.getType());
        assertEquals(NotificationEntityType.ORDER, n.getRelatedEntityType());
        assertEquals(orderId, n.getRelatedEntityId());
        assertTrue(n.getMessage().contains("comprador"), "mensagem deve citar quem agiu");
        assertTrue(n.getMessage().contains("Caneca Azul"), "mensagem deve citar o produto");
        assertEquals(0, notificationRepository.countByRecipientIdAndReadFalse(comprador.getUserId()),
                "o autor da ação não é notificado");
    }

    @Test
    void confirmarPagamento_notificaComprador() throws Exception {
        User vendedor = givenUser("vendedor", "5584999998888");
        User comprador = givenUser("comprador", "5584999990000");
        Product product = givenProduct(vendedor, "Caneca Azul", "ceramica");
        UUID orderId = criaPedido(comprador, product.getProductId());

        mockMvc.perform(post("/orders/" + orderId + "/confirm-payment").with(authAs(vendedor)))
                .andExpect(status().isOk());

        assertEquals(1, notificationOfType(comprador, NotificationType.PAYMENT_CONFIRMED));
    }

    @Test
    void cancelar_notificaAContraparte_naoOAutor() throws Exception {
        User vendedor = givenUser("vendedor", "5584999998888");
        User comprador = givenUser("comprador", "5584999990000");
        Product product = givenProduct(vendedor, "Caneca Azul", "ceramica");
        UUID orderId = criaPedido(comprador, product.getProductId());

        // comprador cancela -> vendedor é a contraparte notificada
        mockMvc.perform(post("/orders/" + orderId + "/cancel").with(authAs(comprador)))
                .andExpect(status().isOk());

        assertEquals(1, notificationOfType(vendedor, NotificationType.ORDER_CANCELLED));
        // comprador (autor do cancelamento) não recebe nada por isso
        assertEquals(0, notificationOfType(comprador, NotificationType.ORDER_CANCELLED));
    }

    // ---- endpoints ----

    @Test
    void listar_semToken_retorna401() throws Exception {
        mockMvc.perform(get("/notifications")).andExpect(status().isUnauthorized());
    }

    @Test
    void listar_eContarNaoLidas_escopadoAoUsuario() throws Exception {
        User ana = givenUser("ana", "5584999991111");
        User bia = givenUser("bia", "5584999992222");
        givenNotification(ana, false);
        givenNotification(ana, true);
        givenNotification(bia, false);

        mockMvc.perform(get("/notifications").with(authAs(ana)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", org.hamcrest.Matchers.hasSize(2)));

        mockMvc.perform(get("/notifications/unread-count").with(authAs(ana)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(1));
    }

    @Test
    void marcarComoLida_zeraContador() throws Exception {
        User ana = givenUser("ana", "5584999991111");
        Notification n = givenNotification(ana, false);

        mockMvc.perform(put("/notifications/" + n.getId() + "/read").with(authAs(ana)))
                .andExpect(status().isNoContent());

        assertEquals(0, notificationRepository.countByRecipientIdAndReadFalse(ana.getUserId()));
    }

    @Test
    void marcarComoLida_deOutroUsuario_retorna404() throws Exception {
        User ana = givenUser("ana", "5584999991111");
        User bia = givenUser("bia", "5584999992222");
        Notification daBia = givenNotification(bia, false);

        mockMvc.perform(put("/notifications/" + daBia.getId() + "/read").with(authAs(ana)))
                .andExpect(status().isNotFound());
    }

    @Test
    void marcarTodasComoLidas() throws Exception {
        User ana = givenUser("ana", "5584999991111");
        givenNotification(ana, false);
        givenNotification(ana, false);

        mockMvc.perform(put("/notifications/read-all").with(authAs(ana)))
                .andExpect(status().isNoContent());

        assertEquals(0, notificationRepository.countByRecipientIdAndReadFalse(ana.getUserId()));
    }

    // ---- helpers ----

    private long notificationOfType(User recipient, NotificationType type) {
        return notificationRepository
                .findByRecipientIdOrderByCreatedAtDesc(recipient.getUserId(), org.springframework.data.domain.Pageable.unpaged())
                .getContent().stream()
                .filter(n -> n.getType() == type)
                .count();
    }

    private Notification givenNotification(User recipient, boolean read) {
        Notification n = new Notification();
        n.setRecipientId(recipient.getUserId());
        n.setType(NotificationType.NEW_ORDER);
        n.setRelatedEntityType(NotificationEntityType.ORDER);
        n.setRelatedEntityId(UUID.randomUUID());
        n.setMessage("mensagem de teste");
        n.setRead(read);
        return notificationRepository.save(n);
    }

    private UUID criaPedido(User buyer, UUID productId) throws Exception {
        String body = """
                {
                  "productId": "%s",
                  "quantity": 1,
                  "freightService": "SEDEX",
                  "freightPrice": 15.00,
                  "address": {
                    "recipientName": "Maria Compradora",
                    "postalCode": "59600000",
                    "street": "Rua das Flores",
                    "number": "100",
                    "district": "Centro",
                    "city": "Mossoró",
                    "state": "RN"
                  }
                }
                """.formatted(productId);
        String resp = mockMvc.perform(post("/orders").with(authAs(buyer))
                        .contentType("application/json").content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return UUID.fromString(objectMapper.readTree(resp).get("orderId").asText());
    }
}
