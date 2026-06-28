package com.taqui.backend;

import com.taqui.backend.modules.user.entity.User;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserControllerIT extends AbstractIntegrationTest {

    // ---- perfil público ----

    @Test
    void perfilPublico_naoVazaEmailNemNumero_expoeHasWhatsapp() throws Exception {
        givenUser("joao", "5584999998888");

        mockMvc.perform(get("/users/joao"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("joao"))
                .andExpect(jsonPath("$.hasWhatsapp").value(true))
                .andExpect(jsonPath("$.email").doesNotExist())
                .andExpect(jsonPath("$.whatsapp").doesNotExist());
    }

    @Test
    void perfilPublico_semWhatsapp_hasWhatsappFalse() throws Exception {
        givenUser("joao", null);

        mockMvc.perform(get("/users/joao"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hasWhatsapp").value(false));
    }

    @Test
    void perfilPublico_inexistente_retorna404() throws Exception {
        mockMvc.perform(get("/users/naoexiste"))
                .andExpect(status().isNotFound());
    }

    // ---- busca ----

    @Test
    void busca_casaUsername_eMin2Chars() throws Exception {
        givenUser("joaozinho", null);

        mockMvc.perform(get("/users").param("q", "joa"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].username").value("joaozinho"));

        mockMvc.perform(get("/users").param("q", "j"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());
    }

    // ---- /users/me ----

    @Test
    void me_semToken_retorna401() throws Exception {
        mockMvc.perform(get("/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void me_comToken_trazEmailEWhatsapp() throws Exception {
        User user = givenUser("joao", "5584999998888");

        mockMvc.perform(get("/users/me").with(authAs(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("joao@test.com"))
                .andExpect(jsonPath("$.whatsapp").value("5584999998888"));
    }

    @Test
    void updateMe_setaWhatsapp_retorna200() throws Exception {
        User user = givenUser("joao", null);

        mockMvc.perform(put("/users/me").with(authAs(user))
                        .contentType("application/json")
                        .content("""
                                {"whatsapp":"5584999998888"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.whatsapp").value("5584999998888"));
    }

    @Test
    void updateMe_whatsappInvalido_retorna400() throws Exception {
        User user = givenUser("joao", null);

        mockMvc.perform(put("/users/me").with(authAs(user))
                        .contentType("application/json")
                        .content("""
                                {"whatsapp":"abc"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateMe_semToken_retorna401() throws Exception {
        mockMvc.perform(put("/users/me")
                        .contentType("application/json")
                        .content("""
                                {"whatsapp":"5584999998888"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    // ---- revelar contato ----

    @Test
    void revelarContato_semToken_retorna401() throws Exception {
        givenUser("joao", "5584999998888");

        mockMvc.perform(get("/users/joao/whatsapp"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void revelarContato_logado_retornaNumero() throws Exception {
        givenUser("joao", "5584999998888");
        User comprador = givenUser("bob", null);

        mockMvc.perform(get("/users/joao/whatsapp").with(authAs(comprador)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("joao"))
                .andExpect(jsonPath("$.whatsapp").value("5584999998888"));
    }

    @Test
    void revelarContato_vendedorSemNumero_retorna404() throws Exception {
        givenUser("joao", null);
        User comprador = givenUser("bob", null);

        mockMvc.perform(get("/users/joao/whatsapp").with(authAs(comprador)))
                .andExpect(status().isNotFound());
    }
}
