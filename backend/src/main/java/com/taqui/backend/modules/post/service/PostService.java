package com.taqui.backend.modules.post.service;

import com.taqui.backend.modules.audit.entity.AuditAction;
import com.taqui.backend.modules.audit.entity.AuditEntityType;
import com.taqui.backend.modules.audit.service.AuditService;
import com.taqui.backend.modules.post.dto.PostRequestDTO;
import com.taqui.backend.modules.post.entity.Post;
import com.taqui.backend.modules.post.exception.InvalidPostException;
import com.taqui.backend.modules.post.exception.PostNotFoundException;
import com.taqui.backend.modules.post.mapper.PostMapper;
import com.taqui.backend.modules.post.repository.PostRepository;
import com.taqui.backend.modules.product.entity.Product;
import com.taqui.backend.modules.product.service.ProductService;
import com.taqui.backend.modules.storage.service.StorageService;
import com.taqui.backend.modules.user.exception.UserNotFoundException;
import com.taqui.backend.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class PostService {

    private final PostRepository postRepository;
    private final PostMapper postMapper;
    private final UserRepository userRepository;
    private final ProductService productService;
    private final StorageService storageService;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public Post findPostById(UUID postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("Postagem não encontrada " + postId));
    }

    @Transactional(readOnly = true)
    public Page<Post> findAllByOrderByCreatedAtDesc(String owner, Pageable pageable) {
        if (owner == null || owner.isBlank()) {
            return postRepository.findAllByOrderByCreatedAtDesc(pageable);
        }
        if (!userRepository.existsByUsername(owner)) {
            throw new UserNotFoundException("Usuário não encontrado: " + owner);
        }
        return postRepository.findByOwner_UsernameOrderByCreatedAtDesc(owner, pageable);
    }

    @Transactional
    public Post createPost(PostRequestDTO dto, UUID ownerId) {
        boolean isAnuncio = dto.productId() != null;
        validateContent(dto.content(), dto.imageUrl(), isAnuncio);

        Post post = postMapper.toEntity(dto);
        post.setOwner(userRepository.getReferenceById(ownerId));

        if (isAnuncio) {
            Product product = productService.findProductById(dto.productId());
            if (!product.getOwner().getUserId().equals(ownerId)) {
                throw new AccessDeniedException("Você só pode anunciar produtos seus");
            }
            post.setProduct(product);
        }
        Post saved = postRepository.save(post);
        auditService.record(ownerId, AuditAction.CREATE, AuditEntityType.POST, saved.getPostId());
        return saved;
    }

    @Transactional
    public Post updatePost(PostRequestDTO dto, UUID ownerId, UUID postId) {
        Post post = findPostById(postId);
        checkOwnership(post, ownerId);

        boolean isAnuncio = post.getProduct() != null;   // o tipo do post não muda no update
        validateContent(dto.content(), dto.imageUrl(), isAnuncio);

        String oldImage = post.getImageUrl();
        String oldThumb = post.getThumbnailUrl();
        postMapper.updateEntityFromDTO(dto, post);        // productId é ignorado de propósito

        // Trocou por uma imagem nova e diferente? A antiga vira órfã no bucket -> apaga.
        String newImage = post.getImageUrl();
        if (newImage != null && !newImage.isBlank() && !newImage.equals(oldImage)) {
            storageService.deleteObject(oldImage);
            storageService.deleteObject(oldThumb);
        }
        auditService.record(ownerId, AuditAction.UPDATE, AuditEntityType.POST, postId);
        return post;
    }

    @Transactional
    public void deletePost(UUID postId, UUID ownerId) {
        Post post = findPostById(postId);
        checkOwnership(post, ownerId);
        auditService.record(ownerId, AuditAction.DELETE, AuditEntityType.POST, postId);
        storageService.deleteObject(post.getImageUrl());
        storageService.deleteObject(post.getThumbnailUrl());
        postRepository.delete(post);
    }

    private void validateContent(String content, String imageUrl, boolean isAnuncio) {
        boolean semTexto = content == null || content.isBlank();
        boolean semImagem = imageUrl == null || imageUrl.isBlank();
        if (!isAnuncio && semTexto && semImagem) {
            throw new InvalidPostException("O post deve ter algum conteúdo");
        }
    }

    private void checkOwnership(Post post, UUID ownerId) {
        if (!post.getOwner().getUserId().equals(ownerId)) {
            throw new AccessDeniedException("Você não é o dono desse post!");
        }
    }
}
