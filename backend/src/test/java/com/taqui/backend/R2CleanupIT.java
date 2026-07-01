package com.taqui.backend;

import com.taqui.backend.modules.post.entity.Post;
import com.taqui.backend.modules.product.entity.Product;
import com.taqui.backend.modules.user.entity.User;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class R2CleanupIT extends AbstractIntegrationTest {

    private Product produtoComImagem(String image, String thumb) {
        User dono = givenUser("dono", "5584999998888");
        Product p = givenProduct(dono, "Caneca", "cerâmica");
        p.setImageUrl(image);
        p.setThumbnailUrl(thumb);
        return productRepository.save(p);
    }

    @Test
    void deletarProduto_apagaImagemEThumbNoBucket() throws Exception {
        Product p = produtoComImagem("https://cdn.taqui/img.png", "https://cdn.taqui/thumb.jpeg");

        mockMvc.perform(delete("/products/" + p.getProductId()).with(authAs(p.getOwner())))
                .andExpect(status().isNoContent());

        verify(storageService).deleteObject("https://cdn.taqui/img.png");
        verify(storageService).deleteObject("https://cdn.taqui/thumb.jpeg");
    }

    @Test
    void editarProduto_trocandoImagem_apagaAAntiga() throws Exception {
        Product p = produtoComImagem("https://cdn.taqui/old.png", "https://cdn.taqui/old-thumb.jpeg");

        mockMvc.perform(put("/products/" + p.getProductId()).with(authAs(p.getOwner()))
                        .contentType("application/json")
                        .content("""
                                {"productName":"Caneca","price":20,"imageUrl":"https://cdn.taqui/new.png","thumbnailUrl":"https://cdn.taqui/new-thumb.jpeg"}
                                """))
                .andExpect(status().isOk());

        verify(storageService).deleteObject("https://cdn.taqui/old.png");
        verify(storageService).deleteObject("https://cdn.taqui/old-thumb.jpeg");
    }

    @Test
    void editarProduto_semTrocarImagem_naoApagaNada() throws Exception {
        Product p = produtoComImagem("https://cdn.taqui/keep.png", "https://cdn.taqui/keep-thumb.jpeg");

        mockMvc.perform(put("/products/" + p.getProductId()).with(authAs(p.getOwner()))
                        .contentType("application/json")
                        .content("""
                                {"productName":"Caneca nova","price":30,"imageUrl":"https://cdn.taqui/keep.png","thumbnailUrl":"https://cdn.taqui/keep-thumb.jpeg"}
                                """))
                .andExpect(status().isOk());

        verify(storageService, never()).deleteObject(any());
    }

    @Test
    void deletarPost_apagaImagemEThumbNoBucket() throws Exception {
        User dono = givenUser("dono", "5584999998888");
        Post post = new Post();
        post.setOwner(dono);
        post.setContent("olha isso");
        post.setImageUrl("https://cdn.taqui/post.png");
        post.setThumbnailUrl("https://cdn.taqui/post-thumb.jpeg");
        post = postRepository.save(post);

        mockMvc.perform(delete("/posts/" + post.getPostId()).with(authAs(dono)))
                .andExpect(status().isNoContent());

        verify(storageService).deleteObject("https://cdn.taqui/post.png");
        verify(storageService).deleteObject("https://cdn.taqui/post-thumb.jpeg");
    }
}
