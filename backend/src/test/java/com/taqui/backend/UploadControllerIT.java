package com.taqui.backend;

import com.taqui.backend.modules.user.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UploadControllerIT extends AbstractIntegrationTest {

    @Test
    void upload_semToken_retorna401() throws Exception {
        MockMultipartFile file =
                new MockMultipartFile("file", "foto.png", "image/png", new byte[]{1, 2, 3});

        mockMvc.perform(multipart("/uploads").file(file))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void upload_contentTypeInvalido_retorna400() throws Exception {
        User uploader = givenUser("uploader", "5584999998888");
        MockMultipartFile file =
                new MockMultipartFile("file", "doc.pdf", "application/pdf", new byte[]{1, 2, 3});

        mockMvc.perform(multipart("/uploads").file(file).with(authAs(uploader)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void upload_arquivoVazio_retorna400() throws Exception {
        User uploader = givenUser("uploader", "5584999998888");
        MockMultipartFile file =
                new MockMultipartFile("file", "vazio.png", "image/png", new byte[0]);

        mockMvc.perform(multipart("/uploads").file(file).with(authAs(uploader)))
                .andExpect(status().isBadRequest());
    }
}
