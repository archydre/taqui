package com.taqui.backend;

import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerIT extends AbstractIntegrationTest {

    @Test
    void register_valido_retorna201_eNaoVazaSenha() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType("application/json")
                        .content("""
                                {"email":"joao@test.com","password":"senha12345","username":"joao","displayName":"João"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("joao"))
                .andExpect(jsonPath("$.email").value("joao@test.com"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void register_emailDuplicado_retorna409() throws Exception {
        givenUser("joao", null); // ocupa joao@test.com

        mockMvc.perform(post("/auth/register")
                        .contentType("application/json")
                        .content("""
                                {"email":"joao@test.com","password":"senha12345","username":"outro","displayName":"Outro"}
                                """))
                .andExpect(status().isConflict());
    }

    @Test
    void register_usernameReservado_retorna409() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType("application/json")
                        .content("""
                                {"email":"a@test.com","password":"senha12345","username":"admin","displayName":"Admin"}
                                """))
                .andExpect(status().isConflict());
    }

    @Test
    void register_usernameInvalido_retorna400() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType("application/json")
                        .content("""
                                {"email":"a@test.com","password":"senha12345","username":"Joao","displayName":"Joao"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_credenciaisCorretas_retorna200ComToken() throws Exception {
        givenUser("maria", null);

        mockMvc.perform(post("/auth/login")
                        .contentType("application/json")
                        .content("""
                                {"email":"maria@test.com","password":"senha12345"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void login_senhaErrada_retorna401() throws Exception {
        givenUser("maria", null);

        mockMvc.perform(post("/auth/login")
                        .contentType("application/json")
                        .content("""
                                {"email":"maria@test.com","password":"senhaERRADA"}
                                """))
                .andExpect(status().isUnauthorized());
    }
}
