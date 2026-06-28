package com.taqui.backend;

import com.taqui.backend.modules.product.entity.Product;
import com.taqui.backend.modules.user.entity.User;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PostControllerIT extends AbstractIntegrationTest {

    // ---- criação: comum ----

    @Test
    void criarComum_comConteudo_retorna201_tipoCOMUM() throws Exception {
        User user = givenUser("joao", null); // postar não exige whatsapp

        mockMvc.perform(post("/posts").with(authAs(user))
                        .contentType("application/json")
                        .content("""
                                {"content":"olá mundo"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("COMUM"))
                .andExpect(jsonPath("$.content").value("olá mundo"))
                .andExpect(jsonPath("$.product").doesNotExist());
    }

    @Test
    void criarComum_vazio_retorna400() throws Exception {
        User user = givenUser("joao", null);

        mockMvc.perform(post("/posts").with(authAs(user))
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void criar_semToken_retorna401() throws Exception {
        mockMvc.perform(post("/posts").contentType("application/json")
                        .content("""
                                {"content":"oi"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    // ---- criação: anúncio ----

    @Test
    void criarAnuncio_produtoProprio_retorna201_tipoANUNCIO() throws Exception {
        User user = givenUser("joao", "5584999998888");
        Product product = givenProduct(user, "Caneca Azul", "ceramica");

        mockMvc.perform(post("/posts").with(authAs(user))
                        .contentType("application/json")
                        .content("{\"productId\":\"" + product.getProductId() + "\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("ANUNCIO"))
                .andExpect(jsonPath("$.product.productName").value("Caneca Azul"));
    }

    @Test
    void criarAnuncio_produtoDeOutro_retorna403() throws Exception {
        User dono = givenUser("joao", "5584999998888");
        User outro = givenUser("bob", "5584999996666");
        Product product = givenProduct(dono, "Caneca Azul", "ceramica");

        mockMvc.perform(post("/posts").with(authAs(outro))
                        .contentType("application/json")
                        .content("{\"productId\":\"" + product.getProductId() + "\"}"))
                .andExpect(status().isForbidden());
    }

    // ---- feed ----

    @Test
    void feed_publico_retornaPage() throws Exception {
        User user = givenUser("joao", null);
        mockMvc.perform(post("/posts").with(authAs(user))
                .contentType("application/json").content("{\"content\":\"oi\"}"));

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));
    }

    @Test
    void feed_filtraPorOwner_inexistente_retorna404() throws Exception {
        mockMvc.perform(get("/posts").param("owner", "naoexiste"))
                .andExpect(status().isNotFound());
    }

    // ---- ownership ----

    @Test
    void atualizar_naoDono_retorna403() throws Exception {
        User dono = givenUser("joao", null);
        User outro = givenUser("bob", null);
        String corpo = "{\"content\":\"original\"}";
        String location = mockMvc.perform(post("/posts").with(authAs(dono))
                        .contentType("application/json").content(corpo))
                .andReturn().getResponse().getContentAsString();
        String postId = objectMapper.readTree(location).get("postId").asText();

        mockMvc.perform(put("/posts/" + postId).with(authAs(outro))
                        .contentType("application/json")
                        .content("{\"content\":\"editado\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void deletar_dono_retorna204() throws Exception {
        User dono = givenUser("joao", null);
        String corpo = "{\"content\":\"vou apagar\"}";
        String resp = mockMvc.perform(post("/posts").with(authAs(dono))
                        .contentType("application/json").content(corpo))
                .andReturn().getResponse().getContentAsString();
        String postId = objectMapper.readTree(resp).get("postId").asText();

        mockMvc.perform(delete("/posts/" + postId).with(authAs(dono)))
                .andExpect(status().isNoContent());
    }
}
