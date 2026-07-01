package com.taqui.backend;

import com.taqui.backend.modules.order.entity.Order;
import com.taqui.backend.modules.order.entity.OrderStatus;
import com.taqui.backend.modules.product.entity.Product;
import com.taqui.backend.modules.user.entity.User;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PixQrIT extends AbstractIntegrationTest {

    @Test
    void pixQr_participante_retornaCopiaECola() throws Exception {
        User vendedor = givenUser("vendedor", "5584999998888");
        User comprador = givenUser("comprador", "5584999990000");
        Product product = givenProduct(vendedor, "Caneca", "ceramica");
        Order order = givenOrder(comprador, product, OrderStatus.AGUARDANDO_PAGAMENTO);

        mockMvc.perform(get("/orders/" + order.getOrderId() + "/pix-qr").with(authAs(comprador)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.copyPaste").value(startsWith("000201")))
                .andExpect(jsonPath("$.amount").exists());
    }

    @Test
    void pixQr_naoParticipante_retorna403() throws Exception {
        User vendedor = givenUser("vendedor", "5584999998888");
        User comprador = givenUser("comprador", "5584999990000");
        User intruso = givenUser("intruso", "5584999997777");
        Product product = givenProduct(vendedor, "Caneca", "ceramica");
        Order order = givenOrder(comprador, product, OrderStatus.AGUARDANDO_PAGAMENTO);

        mockMvc.perform(get("/orders/" + order.getOrderId() + "/pix-qr").with(authAs(intruso)))
                .andExpect(status().isForbidden());
    }

    @Test
    void pixQr_semToken_retorna401() throws Exception {
        mockMvc.perform(get("/orders/" + UUID.randomUUID() + "/pix-qr"))
                .andExpect(status().isUnauthorized());
    }
}
