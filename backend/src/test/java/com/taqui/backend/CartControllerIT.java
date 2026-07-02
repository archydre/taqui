package com.taqui.backend;

import com.taqui.backend.modules.product.entity.Product;
import com.taqui.backend.modules.user.entity.User;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CartControllerIT extends AbstractIntegrationTest {

    private String addBody(UUID productId, int quantity) {
        return """
                { "productId": "%s", "quantity": %d }
                """.formatted(productId, quantity);
    }

    // ---- adicionar ----

    @Test
    void adicionar_semToken_retorna401() throws Exception {
        mockMvc.perform(post("/cart/items").contentType("application/json")
                        .content(addBody(UUID.randomUUID(), 1)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adicionar_valido_retorna200_ePersiste() throws Exception {
        User vendedor = givenUser("vendedor", "5584999998888");
        User comprador = givenUser("comprador", "5584999990000");
        Product product = givenProduct(vendedor, "Caneca Azul", "ceramica");

        mockMvc.perform(post("/cart/items").with(authAs(comprador))
                        .contentType("application/json").content(addBody(product.getProductId(), 2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.quantity").value(2))
                .andExpect(jsonPath("$.product.productName").value("Caneca Azul"));

        assertEquals(1, cartItemRepository.findByBuyer_UserIdOrderByCreatedAtDesc(comprador.getUserId()).size());
    }

    @Test
    void adicionar_mesmoProduto_somaQuantidade() throws Exception {
        User vendedor = givenUser("vendedor", "5584999998888");
        User comprador = givenUser("comprador", "5584999990000");
        Product product = givenProduct(vendedor, "Caneca Azul", "ceramica");

        mockMvc.perform(post("/cart/items").with(authAs(comprador))
                        .contentType("application/json").content(addBody(product.getProductId(), 2)))
                .andExpect(status().isOk());
        mockMvc.perform(post("/cart/items").with(authAs(comprador))
                        .contentType("application/json").content(addBody(product.getProductId(), 3)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(5));

        assertEquals(1, cartItemRepository.findByBuyer_UserIdOrderByCreatedAtDesc(comprador.getUserId()).size());
    }

    @Test
    void adicionar_corpoInvalido_retorna400() throws Exception {
        User comprador = givenUser("comprador", "5584999990000");

        mockMvc.perform(post("/cart/items").with(authAs(comprador))
                        .contentType("application/json").content(addBody(UUID.randomUUID(), 0)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void adicionar_produtoInexistente_retorna404() throws Exception {
        User comprador = givenUser("comprador", "5584999990000");

        mockMvc.perform(post("/cart/items").with(authAs(comprador))
                        .contentType("application/json")
                        .content(addBody(UUID.fromString("00000000-0000-0000-0000-000000000000"), 1)))
                .andExpect(status().isNotFound());
    }

    @Test
    void adicionar_proprioProduto_retorna409() throws Exception {
        User vendedor = givenUser("vendedor", "5584999998888");
        Product product = givenProduct(vendedor, "Caneca Azul", "ceramica");

        mockMvc.perform(post("/cart/items").with(authAs(vendedor))
                        .contentType("application/json").content(addBody(product.getProductId(), 1)))
                .andExpect(status().isConflict());
    }

    // ---- listar ----

    @Test
    void listar_retornaSoDoComprador() throws Exception {
        User vendedor = givenUser("vendedor", "5584999998888");
        User comprador = givenUser("comprador", "5584999990000");
        User outro = givenUser("outro", "5584999991111");
        Product product = givenProduct(vendedor, "Caneca Azul", "ceramica");
        givenCartItem(comprador, product, 1);
        givenCartItem(outro, product, 4);

        mockMvc.perform(get("/cart").with(authAs(comprador)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].product.productName").value("Caneca Azul"))
                .andExpect(jsonPath("$[0].quantity").value(1));
    }

    // ---- atualizar quantidade ----

    @Test
    void atualizar_alteraQuantidade() throws Exception {
        User vendedor = givenUser("vendedor", "5584999998888");
        User comprador = givenUser("comprador", "5584999990000");
        Product product = givenProduct(vendedor, "Caneca Azul", "ceramica");
        givenCartItem(comprador, product, 1);

        mockMvc.perform(patch("/cart/items/{productId}", product.getProductId()).with(authAs(comprador))
                        .contentType("application/json").content("{\"quantity\": 7}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(7));
    }

    @Test
    void atualizar_itemForaDoCarrinho_retorna404() throws Exception {
        User comprador = givenUser("comprador", "5584999990000");

        mockMvc.perform(patch("/cart/items/{productId}", UUID.randomUUID()).with(authAs(comprador))
                        .contentType("application/json").content("{\"quantity\": 2}"))
                .andExpect(status().isNotFound());
    }

    // ---- remover ----

    @Test
    void remover_doCarrinho_retorna204_eRemove() throws Exception {
        User vendedor = givenUser("vendedor", "5584999998888");
        User comprador = givenUser("comprador", "5584999990000");
        Product product = givenProduct(vendedor, "Caneca Azul", "ceramica");
        givenCartItem(comprador, product, 1);

        mockMvc.perform(delete("/cart/items/{productId}", product.getProductId()).with(authAs(comprador)))
                .andExpect(status().isNoContent());

        assertEquals(0, cartItemRepository.findByBuyer_UserIdOrderByCreatedAtDesc(comprador.getUserId()).size());
    }

    @Test
    void remover_itemForaDoCarrinho_retorna404() throws Exception {
        User comprador = givenUser("comprador", "5584999990000");

        mockMvc.perform(delete("/cart/items/{productId}", UUID.randomUUID()).with(authAs(comprador)))
                .andExpect(status().isNotFound());
    }
}
