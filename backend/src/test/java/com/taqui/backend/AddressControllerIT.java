package com.taqui.backend;

import com.taqui.backend.modules.address.entity.UserAddress;
import com.taqui.backend.modules.user.entity.User;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AddressControllerIT extends AbstractIntegrationTest {

    private static final String ADDRESS_BODY = """
            {
              "recipientName": "Maria Compradora",
              "postalCode": "59600000",
              "street": "Rua das Flores",
              "number": "100",
              "district": "Centro",
              "city": "Mossoró",
              "state": "RN"
            }
            """;

    @Test
    void listar_semToken_retorna401() throws Exception {
        mockMvc.perform(get("/addresses"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void criar_valido_retorna201_ePersiste() throws Exception {
        User dono = givenUser("dono", "5584999990000");

        mockMvc.perform(post("/addresses").with(authAs(dono))
                        .contentType("application/json").content(ADDRESS_BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.recipientName").value("Maria Compradora"))
                .andExpect(jsonPath("$.postalCode").value("59600000"))
                .andExpect(jsonPath("$.city").value("Mossoró"));

        assertEquals(1, userAddressRepository.findByOwner_UserIdOrderByCreatedAtDesc(dono.getUserId()).size());
    }

    @Test
    void criar_corpoInvalido_retorna400() throws Exception {
        User dono = givenUser("dono", "5584999990000");

        mockMvc.perform(post("/addresses").with(authAs(dono))
                        .contentType("application/json").content("{\"postalCode\":\"123\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listar_retornaSoDoDono() throws Exception {
        User dono = givenUser("dono", "5584999990000");
        User outro = givenUser("outro", "5584999991111");
        givenSavedAddress(dono);
        givenSavedAddress(outro);

        mockMvc.perform(get("/addresses").with(authAs(dono)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].recipientName").value("Maria Compradora"));
    }

    @Test
    void apagar_doDono_retorna204_eRemove() throws Exception {
        User dono = givenUser("dono", "5584999990000");
        UserAddress saved = givenSavedAddress(dono);

        mockMvc.perform(delete("/addresses/{id}", saved.getAddressId()).with(authAs(dono)))
                .andExpect(status().isNoContent());

        assertEquals(0, userAddressRepository.findByOwner_UserIdOrderByCreatedAtDesc(dono.getUserId()).size());
    }

    @Test
    void apagar_deOutro_retorna403() throws Exception {
        User dono = givenUser("dono", "5584999990000");
        User intruso = givenUser("intruso", "5584999991111");
        UserAddress saved = givenSavedAddress(dono);

        mockMvc.perform(delete("/addresses/{id}", saved.getAddressId()).with(authAs(intruso)))
                .andExpect(status().isForbidden());

        assertEquals(1, userAddressRepository.findByOwner_UserIdOrderByCreatedAtDesc(dono.getUserId()).size());
    }

    @Test
    void apagar_inexistente_retorna404() throws Exception {
        User dono = givenUser("dono", "5584999990000");

        mockMvc.perform(delete("/addresses/{id}", UUID.randomUUID()).with(authAs(dono)))
                .andExpect(status().isNotFound());
    }
}
