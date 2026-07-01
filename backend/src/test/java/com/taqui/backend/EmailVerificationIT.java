package com.taqui.backend;

import com.taqui.backend.modules.user.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class EmailVerificationIT extends AbstractIntegrationTest {

    @Test
    void register_geraTokenEDeixaEmailNaoVerificado() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType("application/json")
                        .content("""
                                {"email":"novo@test.com","password":"senha12345","username":"novo","displayName":"Novo"}
                                """))
                .andExpect(status().isCreated());

        User saved = userRepository.findByEmail("novo@test.com").orElseThrow();
        assertThat(saved.isEmailVerified()).isFalse();
        assertThat(saved.getVerificationToken()).isNotBlank();
        assertThat(saved.getVerificationTokenExpiresAt()).isAfter(Instant.now());
    }

    @Test
    void register_publicaNaFilaDeVerificacao() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType("application/json")
                        .content("""
                                {"email":"fila@test.com","password":"senha12345","username":"fila","displayName":"Fila"}
                                """))
                .andExpect(status().isCreated());

        verify(rabbitTemplate).sendAndReceive(eq("email.verify.request"), any(Message.class));
    }

    @Test
    void verify_tokenValido_confirmaEmailELimpaToken() throws Exception {
        User user = givenUser("carlos", null);
        user.setVerificationToken("tok-valido");
        user.setVerificationTokenExpiresAt(Instant.now().plus(1, ChronoUnit.HOURS));
        userRepository.save(user);

        mockMvc.perform(post("/auth/verify").param("token", "tok-valido"))
                .andExpect(status().isNoContent());

        User updated = userRepository.findByEmail("carlos@test.com").orElseThrow();
        assertThat(updated.isEmailVerified()).isTrue();
        assertThat(updated.getVerificationToken()).isNull();
        assertThat(updated.getVerificationTokenExpiresAt()).isNull();
    }

    @Test
    void verify_tokenInexistente_retorna400() throws Exception {
        mockMvc.perform(post("/auth/verify").param("token", "nao-existe"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void verify_tokenExpirado_retorna400_eNaoConfirma() throws Exception {
        User user = givenUser("mariana", null);
        user.setVerificationToken("tok-expirado");
        user.setVerificationTokenExpiresAt(Instant.now().minus(1, ChronoUnit.HOURS));
        userRepository.save(user);

        mockMvc.perform(post("/auth/verify").param("token", "tok-expirado"))
                .andExpect(status().isBadRequest());

        User notUpdated = userRepository.findByEmail("mariana@test.com").orElseThrow();
        assertThat(notUpdated.isEmailVerified()).isFalse();
    }
}
