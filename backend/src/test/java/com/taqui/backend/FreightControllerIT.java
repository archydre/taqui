package com.taqui.backend;

import com.taqui.backend.modules.product.entity.Product;
import com.taqui.backend.modules.user.entity.User;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class FreightControllerIT extends AbstractIntegrationTest {

    private static final String BODY = "{\"toPostalCode\":\"59000000\",\"quantity\":2}";

    @Test
    void quote_semToken_retorna401() throws Exception {
        mockMvc.perform(post("/freight/quote/product/" + UUID.randomUUID())
                        .contentType("application/json").content(BODY))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void quote_produtoInexistente_retorna404() throws Exception {
        User comprador = givenUser("comprador", "5584999990000");

        mockMvc.perform(post("/freight/quote/product/" + UUID.randomUUID()).with(authAs(comprador))
                        .contentType("application/json").content(BODY))
                .andExpect(status().isNotFound());
    }

    @Test
    void quote_vendedorSemCep_retorna422() throws Exception {
        User vendedor = givenUser("vendedor", "5584999998888");
        User comprador = givenUser("comprador", "5584999990000");
        Product product = givenProduct(vendedor, "Caneca", "ceramica");

        mockMvc.perform(post("/freight/quote/product/" + product.getProductId()).with(authAs(comprador))
                        .contentType("application/json").content(BODY))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void quote_produtoSemDimensoes_retorna422() throws Exception {
        User vendedor = givenUser("vendedor", "5584999998888");
        vendedor.setPostalCode("59600000");
        userRepository.save(vendedor);
        User comprador = givenUser("comprador", "5584999990000");
        Product product = givenProduct(vendedor, "Caneca", "ceramica");

        mockMvc.perform(post("/freight/quote/product/" + product.getProductId()).with(authAs(comprador))
                        .contentType("application/json").content(BODY))
                .andExpect(status().isUnprocessableEntity());
    }
}
