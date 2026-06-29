package com.taqui.backend;

import com.taqui.backend.modules.order.entity.Order;
import com.taqui.backend.modules.order.entity.OrderStatus;
import com.taqui.backend.modules.product.entity.Product;
import com.taqui.backend.modules.user.entity.User;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OrderControllerIT extends AbstractIntegrationTest {

    private String orderBody(UUID productId) {
        return """
                {
                  "productId": "%s",
                  "quantity": 2,
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
    }

    // ---- criação + portões ----

    @Test
    void criar_semToken_retorna401() throws Exception {
        mockMvc.perform(post("/orders").contentType("application/json")
                        .content(orderBody(UUID.randomUUID())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void criar_valido_retorna201_comSnapshotsEStatus() throws Exception {
        User vendedor = givenUser("vendedor", "5584999998888");
        User comprador = givenUser("comprador", "5584999990000");
        Product product = givenProduct(vendedor, "Caneca Azul", "ceramica");

        String resp = mockMvc.perform(post("/orders").with(authAs(comprador))
                        .contentType("application/json").content(orderBody(product.getProductId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").exists())
                .andExpect(jsonPath("$.status").value("AGUARDANDO_PAGAMENTO"))
                .andExpect(jsonPath("$.quantity").value(2))
                .andExpect(jsonPath("$.freightService").value("SEDEX"))
                .andExpect(jsonPath("$.sellerPixKey").value("chave-pix-teste"))
                .andExpect(jsonPath("$.product.productName").value("Caneca Azul"))
                .andExpect(jsonPath("$.buyer.username").value("comprador"))
                .andReturn().getResponse().getContentAsString();

        BigDecimal total = objectMapper.readTree(resp).get("total").decimalValue();
        assertEquals(0, total.compareTo(new BigDecimal("55.00")));
    }

    @Test
    void criar_corpoInvalido_retorna400() throws Exception {
        User comprador = givenUser("comprador", "5584999990000");

        mockMvc.perform(post("/orders").with(authAs(comprador))
                        .contentType("application/json").content("{\"quantity\":0}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void criar_produtoInexistente_retorna404() throws Exception {
        User comprador = givenUser("comprador", "5584999990000");

        mockMvc.perform(post("/orders").with(authAs(comprador))
                        .contentType("application/json")
                        .content(orderBody(UUID.fromString("00000000-0000-0000-0000-000000000000"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void criar_vendedorSemPix_retorna422() throws Exception {
        User vendedorSemPix = givenUser("vendedor", "5584999998888", null);
        User comprador = givenUser("comprador", "5584999990000");
        Product product = givenProduct(vendedorSemPix, "Caneca Azul", "ceramica");

        mockMvc.perform(post("/orders").with(authAs(comprador))
                        .contentType("application/json").content(orderBody(product.getProductId())))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void criar_proprioProduto_retorna409() throws Exception {
        User vendedor = givenUser("vendedor", "5584999998888");
        Product product = givenProduct(vendedor, "Caneca Azul", "ceramica");

        mockMvc.perform(post("/orders").with(authAs(vendedor))
                        .contentType("application/json").content(orderBody(product.getProductId())))
                .andExpect(status().isConflict());
    }

    // ---- listagens ----

    @Test
    void meusPedidos_listaSoOsDoComprador() throws Exception {
        User vendedor = givenUser("vendedor", "5584999998888");
        User comprador = givenUser("comprador", "5584999990000");
        User outro = givenUser("outro", "5584999991111");
        Product product = givenProduct(vendedor, "Caneca Azul", "ceramica");
        givenOrder(comprador, product, OrderStatus.AGUARDANDO_PAGAMENTO);
        givenOrder(outro, product, OrderStatus.AGUARDANDO_PAGAMENTO);

        mockMvc.perform(get("/orders").with(authAs(comprador)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].buyer.username").value("comprador"));
    }

    @Test
    void recebidos_listaSoOsDoVendedor() throws Exception {
        User vendedor = givenUser("vendedor", "5584999998888");
        User vendedor2 = givenUser("vendedor2", "5584999992222");
        User comprador = givenUser("comprador", "5584999990000");
        Product doVendedor = givenProduct(vendedor, "Caneca Azul", "ceramica");
        Product doVendedor2 = givenProduct(vendedor2, "Camiseta", "algodao");
        givenOrder(comprador, doVendedor, OrderStatus.AGUARDANDO_PAGAMENTO);
        givenOrder(comprador, doVendedor2, OrderStatus.AGUARDANDO_PAGAMENTO);

        mockMvc.perform(get("/orders/received").with(authAs(vendedor)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].product.productName").value("Caneca Azul"));
    }

    // ---- ver um pedido (authz) ----

    @Test
    void verPedido_compradorEVendedor_200_terceiro_403() throws Exception {
        User vendedor = givenUser("vendedor", "5584999998888");
        User comprador = givenUser("comprador", "5584999990000");
        User terceiro = givenUser("terceiro", "5584999993333");
        Product product = givenProduct(vendedor, "Caneca Azul", "ceramica");
        Order order = givenOrder(comprador, product, OrderStatus.AGUARDANDO_PAGAMENTO);

        mockMvc.perform(get("/orders/" + order.getOrderId()).with(authAs(comprador)))
                .andExpect(status().isOk());
        mockMvc.perform(get("/orders/" + order.getOrderId()).with(authAs(vendedor)))
                .andExpect(status().isOk());
        mockMvc.perform(get("/orders/" + order.getOrderId()).with(authAs(terceiro)))
                .andExpect(status().isForbidden());
    }

    @Test
    void verPedido_inexistente_retorna404() throws Exception {
        User comprador = givenUser("comprador", "5584999990000");

        mockMvc.perform(get("/orders/00000000-0000-0000-0000-000000000000").with(authAs(comprador)))
                .andExpect(status().isNotFound());
    }

    // ---- confirmar pagamento ----

    @Test
    void confirmar_vendedor_marcaPago() throws Exception {
        User vendedor = givenUser("vendedor", "5584999998888");
        User comprador = givenUser("comprador", "5584999990000");
        Product product = givenProduct(vendedor, "Caneca Azul", "ceramica");
        Order order = givenOrder(comprador, product, OrderStatus.AGUARDANDO_PAGAMENTO);

        mockMvc.perform(post("/orders/" + order.getOrderId() + "/confirm-payment").with(authAs(vendedor)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAGO"));
    }

    @Test
    void confirmar_naoVendedor_retorna403() throws Exception {
        User vendedor = givenUser("vendedor", "5584999998888");
        User comprador = givenUser("comprador", "5584999990000");
        Product product = givenProduct(vendedor, "Caneca Azul", "ceramica");
        Order order = givenOrder(comprador, product, OrderStatus.AGUARDANDO_PAGAMENTO);

        mockMvc.perform(post("/orders/" + order.getOrderId() + "/confirm-payment").with(authAs(comprador)))
                .andExpect(status().isForbidden());
    }

    @Test
    void confirmar_pedidoJaPago_retorna409() throws Exception {
        User vendedor = givenUser("vendedor", "5584999998888");
        User comprador = givenUser("comprador", "5584999990000");
        Product product = givenProduct(vendedor, "Caneca Azul", "ceramica");
        Order order = givenOrder(comprador, product, OrderStatus.PAGO);

        mockMvc.perform(post("/orders/" + order.getOrderId() + "/confirm-payment").with(authAs(vendedor)))
                .andExpect(status().isConflict());
    }

    // ---- enviar (ship) ----

    @Test
    void enviar_vendedor_pedidoPago_marcaEnviado() throws Exception {
        User vendedor = givenUser("vendedor", "5584999998888");
        User comprador = givenUser("comprador", "5584999990000");
        Product product = givenProduct(vendedor, "Caneca Azul", "ceramica");
        Order order = givenOrder(comprador, product, OrderStatus.PAGO);

        mockMvc.perform(post("/orders/" + order.getOrderId() + "/ship").with(authAs(vendedor)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ENVIADO"));
    }

    @Test
    void enviar_pedidoNaoPago_retorna409() throws Exception {
        User vendedor = givenUser("vendedor", "5584999998888");
        User comprador = givenUser("comprador", "5584999990000");
        Product product = givenProduct(vendedor, "Caneca Azul", "ceramica");
        Order order = givenOrder(comprador, product, OrderStatus.AGUARDANDO_PAGAMENTO);

        mockMvc.perform(post("/orders/" + order.getOrderId() + "/ship").with(authAs(vendedor)))
                .andExpect(status().isConflict());
    }

    @Test
    void enviar_naoVendedor_retorna403() throws Exception {
        User vendedor = givenUser("vendedor", "5584999998888");
        User comprador = givenUser("comprador", "5584999990000");
        Product product = givenProduct(vendedor, "Caneca Azul", "ceramica");
        Order order = givenOrder(comprador, product, OrderStatus.PAGO);

        mockMvc.perform(post("/orders/" + order.getOrderId() + "/ship").with(authAs(comprador)))
                .andExpect(status().isForbidden());
    }

    // ---- cancelar ----

    @Test
    void cancelar_comprador_marcaCancelado() throws Exception {
        User vendedor = givenUser("vendedor", "5584999998888");
        User comprador = givenUser("comprador", "5584999990000");
        Product product = givenProduct(vendedor, "Caneca Azul", "ceramica");
        Order order = givenOrder(comprador, product, OrderStatus.AGUARDANDO_PAGAMENTO);

        mockMvc.perform(post("/orders/" + order.getOrderId() + "/cancel").with(authAs(comprador)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELADO"));
    }

    @Test
    void cancelar_vendedor_tambemPode() throws Exception {
        User vendedor = givenUser("vendedor", "5584999998888");
        User comprador = givenUser("comprador", "5584999990000");
        Product product = givenProduct(vendedor, "Caneca Azul", "ceramica");
        Order order = givenOrder(comprador, product, OrderStatus.PAGO);

        mockMvc.perform(post("/orders/" + order.getOrderId() + "/cancel").with(authAs(vendedor)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELADO"));
    }

    @Test
    void cancelar_pedidoEnviado_retorna409() throws Exception {
        User vendedor = givenUser("vendedor", "5584999998888");
        User comprador = givenUser("comprador", "5584999990000");
        Product product = givenProduct(vendedor, "Caneca Azul", "ceramica");
        Order order = givenOrder(comprador, product, OrderStatus.ENVIADO);

        mockMvc.perform(post("/orders/" + order.getOrderId() + "/cancel").with(authAs(comprador)))
                .andExpect(status().isConflict());
    }

    @Test
    void cancelar_naoParticipante_retorna403() throws Exception {
        User vendedor = givenUser("vendedor", "5584999998888");
        User comprador = givenUser("comprador", "5584999990000");
        User terceiro = givenUser("terceiro", "5584999993333");
        Product product = givenProduct(vendedor, "Caneca Azul", "ceramica");
        Order order = givenOrder(comprador, product, OrderStatus.AGUARDANDO_PAGAMENTO);

        mockMvc.perform(post("/orders/" + order.getOrderId() + "/cancel").with(authAs(terceiro)))
                .andExpect(status().isForbidden());
    }
}
