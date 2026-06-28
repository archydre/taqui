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

class ProductControllerIT extends AbstractIntegrationTest {

    private static final String CANECA = """
            {"productName":"Caneca Azul","productDescription":"ceramica","price":25.0}
            """;

    // ---- criação + portão de venda ----

    @Test
    void criar_semToken_retorna401() throws Exception {
        mockMvc.perform(post("/products").contentType("application/json").content(CANECA))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void criar_semWhatsapp_retorna422() throws Exception {
        User user = givenUser("joao", null); // sem whatsapp

        mockMvc.perform(post("/products").with(authAs(user))
                        .contentType("application/json").content(CANECA))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void criar_comWhatsapp_retorna201() throws Exception {
        User user = givenUser("joao", "5584999998888");

        mockMvc.perform(post("/products").with(authAs(user))
                        .contentType("application/json").content(CANECA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.productName").value("Caneca Azul"))
                .andExpect(jsonPath("$.owner.username").value("joao"))
                .andExpect(jsonPath("$.owner.hasWhatsapp").value(true));
    }

    @Test
    void criar_corpoInvalido_retorna400() throws Exception {
        User user = givenUser("joao", "5584999998888");

        mockMvc.perform(post("/products").with(authAs(user))
                        .contentType("application/json")
                        .content("""
                                {"productName":"","price":-5}
                                """))
                .andExpect(status().isBadRequest());
    }

    // ---- leitura (pública) ----

    @Test
    void listar_publico_retornaPage() throws Exception {
        User user = givenUser("joao", "5584999998888");
        givenProduct(user, "Caneca Azul", "ceramica");

        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));
    }

    @Test
    void buscarPorId_existente_retorna200_inexistente_retorna404() throws Exception {
        User user = givenUser("joao", "5584999998888");
        Product product = givenProduct(user, "Caneca Azul", "ceramica");

        mockMvc.perform(get("/products/" + product.getProductId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productName").value("Caneca Azul"));

        mockMvc.perform(get("/products/00000000-0000-0000-0000-000000000000"))
                .andExpect(status().isNotFound());
    }

    // ---- filtro por vendedor ----

    @Test
    void filtrarPorOwner_existente_so_dele() throws Exception {
        User joao = givenUser("joao", "5584999998888");
        User maria = givenUser("maria", "5584999997777");
        givenProduct(joao, "Caneca do Joao", "ceramica");
        givenProduct(maria, "Caneca da Maria", "porcelana");

        mockMvc.perform(get("/products").param("owner", "joao"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].productName").value("Caneca do Joao"));
    }

    @Test
    void filtrarPorOwner_inexistente_retorna404() throws Exception {
        mockMvc.perform(get("/products").param("owner", "naoexiste"))
                .andExpect(status().isNotFound());
    }

    // ---- busca textual ----

    @Test
    void buscar_casaNomeOuDescricao() throws Exception {
        User joao = givenUser("joao", "5584999998888");
        givenProduct(joao, "Caneca Azul", "ceramica");
        givenProduct(joao, "Camiseta Preta", "algodao");

        mockMvc.perform(get("/products").param("q", "caneca"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].productName").value("Caneca Azul"));

        mockMvc.perform(get("/products").param("q", "algodao"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].productName").value("Camiseta Preta"));
    }

    // ---- ownership em update/delete ----

    @Test
    void atualizar_naoDono_retorna403_dono_retorna200() throws Exception {
        User dono = givenUser("joao", "5584999998888");
        User outro = givenUser("bob", "5584999996666");
        Product product = givenProduct(dono, "Caneca Azul", "ceramica");
        String novoNome = """
                {"productName":"Caneca Nova","price":30.0}
                """;

        mockMvc.perform(put("/products/" + product.getProductId()).with(authAs(outro))
                        .contentType("application/json").content(novoNome))
                .andExpect(status().isForbidden());

        mockMvc.perform(put("/products/" + product.getProductId()).with(authAs(dono))
                        .contentType("application/json").content(novoNome))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productName").value("Caneca Nova"));
    }

    @Test
    void deletar_dono_retorna204_eSomeDoBanco() throws Exception {
        User dono = givenUser("joao", "5584999998888");
        Product product = givenProduct(dono, "Caneca Azul", "ceramica");

        mockMvc.perform(delete("/products/" + product.getProductId()).with(authAs(dono)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/products/" + product.getProductId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void deletar_naoDono_retorna403() throws Exception {
        User dono = givenUser("joao", "5584999998888");
        User outro = givenUser("bob", "5584999996666");
        Product product = givenProduct(dono, "Caneca Azul", "ceramica");

        mockMvc.perform(delete("/products/" + product.getProductId()).with(authAs(outro)))
                .andExpect(status().isForbidden());
    }
}
