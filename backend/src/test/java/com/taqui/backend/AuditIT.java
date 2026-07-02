package com.taqui.backend;

import com.taqui.backend.modules.audit.entity.AuditAction;
import com.taqui.backend.modules.audit.entity.AuditEntityType;
import com.taqui.backend.modules.audit.entity.AuditLog;
import com.taqui.backend.modules.audit.repository.AuditLogRepository;
import com.taqui.backend.modules.order.entity.Order;
import com.taqui.backend.modules.product.entity.Product;
import com.taqui.backend.modules.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuditIT extends AbstractIntegrationTest {

    @Autowired
    private AuditLogRepository auditLogRepository;

    private static final String CANECA = """
            {"productName":"Caneca Azul","productDescription":"ceramica","price":25.0}
            """;

    @BeforeEach
    void clearAuditLog() {
        auditLogRepository.deleteAll();
    }

    // ---- autoria (created_by / updated_by) ----

    @Test
    void criarProduto_preencheCreatedBy_eGravaTrilha() throws Exception {
        User joao = givenUser("joao", "5584999998888");

        UUID productId = criaProduto(joao);

        Product product = productRepository.findById(productId).orElseThrow();
        assertEquals(joao.getUserId(), product.getCreatedBy());

        AuditLog log = singleLog(productId, AuditAction.CREATE);
        assertEquals(AuditEntityType.PRODUCT, log.getEntityType());
        assertEquals(joao.getUserId(), log.getActorId());
    }

    @Test
    void atualizarProduto_preencheUpdatedBy_eGravaTrilha() throws Exception {
        User joao = givenUser("joao", "5584999998888");
        UUID productId = criaProduto(joao);

        mockMvc.perform(put("/products/" + productId).with(authAs(joao))
                        .contentType("application/json")
                        .content("""
                                {"productName":"Caneca Verde","productDescription":"ceramica","price":30.0}
                                """))
                .andExpect(status().isOk());

        Product product = productRepository.findById(productId).orElseThrow();
        assertEquals(joao.getUserId(), product.getUpdatedBy());
        assertEquals(joao.getUserId(), product.getCreatedBy());

        AuditLog log = singleLog(productId, AuditAction.UPDATE);
        assertEquals(AuditEntityType.PRODUCT, log.getEntityType());
        assertEquals(joao.getUserId(), log.getActorId());
    }

    // ---- trilha sobrevive ao hard delete ----

    @Test
    void deletarProduto_gravaTrilha_mesmoComORegistroApagado() throws Exception {
        User joao = givenUser("joao", "5584999998888");
        UUID productId = criaProduto(joao);

        mockMvc.perform(delete("/products/" + productId).with(authAs(joao)))
                .andExpect(status().isNoContent());

        assertTrue(productRepository.findById(productId).isEmpty());

        AuditLog log = singleLog(productId, AuditAction.DELETE);
        assertEquals(AuditEntityType.PRODUCT, log.getEntityType());
        assertEquals(joao.getUserId(), log.getActorId());
    }

    // ---- ciclo do pedido: cada ação vira uma linha na trilha ----

    @Test
    void cicloPedido_gravaCreateConfirmShip_comOsAtoresCertos() throws Exception {
        User vendedor = givenUser("vendedor", "5584999998888");
        User comprador = givenUser("comprador", "5584999990000");
        Product product = givenProduct(vendedor, "Caneca Azul", "ceramica");

        UUID orderId = criaPedido(comprador, product.getProductId());

        Order order = orderRepository.findById(orderId).orElseThrow();
        assertEquals(comprador.getUserId(), order.getCreatedBy());
        assertEquals(comprador.getUserId(), singleLog(orderId, AuditAction.CREATE).getActorId());

        mockMvc.perform(post("/orders/" + orderId + "/confirm-payment").with(authAs(vendedor)))
                .andExpect(status().isOk());
        assertEquals(vendedor.getUserId(), singleLog(orderId, AuditAction.PAYMENT_CONFIRMED).getActorId());

        mockMvc.perform(post("/orders/" + orderId + "/ship").with(authAs(vendedor)))
                .andExpect(status().isOk());
        AuditLog shipped = singleLog(orderId, AuditAction.SHIPPED);
        assertEquals(AuditEntityType.ORDER, shipped.getEntityType());
        assertEquals(vendedor.getUserId(), shipped.getActorId());
    }

    @Test
    void cancelarPedido_gravaCancelled_comOAtor() throws Exception {
        User vendedor = givenUser("vendedor", "5584999998888");
        User comprador = givenUser("comprador", "5584999990000");
        Product product = givenProduct(vendedor, "Caneca Azul", "ceramica");

        UUID orderId = criaPedido(comprador, product.getProductId());

        mockMvc.perform(post("/orders/" + orderId + "/cancel").with(authAs(comprador)))
                .andExpect(status().isOk());

        AuditLog log = singleLog(orderId, AuditAction.CANCELLED);
        assertEquals(AuditEntityType.ORDER, log.getEntityType());
        assertEquals(comprador.getUserId(), log.getActorId());
    }

    // ---- helpers ----

    private UUID criaProduto(User owner) throws Exception {
        String resp = mockMvc.perform(post("/products").with(authAs(owner))
                        .contentType("application/json").content(CANECA))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return UUID.fromString(objectMapper.readTree(resp).get("productId").asText());
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

    private AuditLog singleLog(UUID entityId, AuditAction action) {
        List<AuditLog> matches = auditLogRepository.findAll().stream()
                .filter(l -> entityId.equals(l.getEntityId()) && l.getAction() == action)
                .toList();
        assertEquals(1, matches.size(), "esperava exatamente 1 log " + action + " para " + entityId);
        return matches.get(0);
    }
}
