package com.taqui.backend.modules.comment.service;

import com.taqui.backend.modules.comment.dto.CommentRequestDTO;
import com.taqui.backend.modules.comment.entity.Comment;
import com.taqui.backend.modules.comment.exception.CommentNotFoundException;
import com.taqui.backend.modules.comment.repository.CommentRepository;
import com.taqui.backend.modules.post.entity.Post;
import com.taqui.backend.modules.post.service.PostService;
import com.taqui.backend.modules.product.entity.Product;
import com.taqui.backend.modules.product.service.ProductService;
import com.taqui.backend.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final PostService postService;
    private final ProductService productService;

    @Transactional(readOnly = true)
    public Page<Comment> findByPost(UUID postId, Pageable pageable) {
        postService.findPostById(postId);
        return commentRepository.findByPost_PostIdOrderByCreatedAtDesc(postId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Comment> findByProduct(UUID productId, Pageable pageable) {
        productService.findProductById(productId);
        return commentRepository.findByProduct_ProductIdOrderByCreatedAtDesc(productId, pageable);
    }

    @Transactional
    public Comment createOnPost(UUID postId, CommentRequestDTO dto, UUID authorId) {
        Post post = postService.findPostById(postId);
        Comment comment = new Comment();
        comment.setAuthor(userRepository.getReferenceById(authorId));
        comment.setContent(dto.content());
        comment.setPost(post);
        return commentRepository.save(comment);
    }

    @Transactional
    public Comment createOnProduct(UUID productId, CommentRequestDTO dto, UUID authorId) {
        Product product = productService.findProductById(productId);
        Comment comment = new Comment();
        comment.setAuthor(userRepository.getReferenceById(authorId));
        comment.setContent(dto.content());
        comment.setProduct(product);
        return commentRepository.save(comment);
    }

    @Transactional
    public void delete(UUID commentId, UUID userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException("Comentário não encontrado: " + commentId));
        if (!canDelete(comment, userId)) {
            throw new AccessDeniedException("Você não pode apagar esse comentário");
        }
        commentRepository.delete(comment);
    }

    private boolean canDelete(Comment comment, UUID userId) {
        if (comment.getAuthor().getUserId().equals(userId)) {
            return true;
        }
        if (comment.getPost() != null && comment.getPost().getOwner().getUserId().equals(userId)) {
            return true;
        }
        return comment.getProduct() != null
                && comment.getProduct().getOwner().getUserId().equals(userId);
    }
}
