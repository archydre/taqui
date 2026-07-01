package com.taqui.backend.modules.comment.controller;

import com.taqui.backend.modules.comment.dto.CommentRequestDTO;
import com.taqui.backend.modules.comment.dto.CommentResponseDTO;
import com.taqui.backend.modules.comment.entity.Comment;
import com.taqui.backend.modules.comment.mapper.CommentMapper;
import com.taqui.backend.modules.comment.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final CommentMapper commentMapper;

    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<Page<CommentResponseDTO>> listPostComments(
            @PathVariable UUID postId,
            Pageable pageable) {
        Page<CommentResponseDTO> body = commentService.findByPost(postId, pageable)
                .map(commentMapper::toResponseDTO);
        return ResponseEntity.ok(body);
    }

    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<CommentResponseDTO> createPostComment(
            @PathVariable UUID postId,
            @Valid @RequestBody CommentRequestDTO commentRequestDTO,
            @AuthenticationPrincipal Jwt jwt) {
        UUID authorId = UUID.fromString(jwt.getSubject());
        Comment comment = commentService.createOnPost(postId, commentRequestDTO, authorId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(commentMapper.toResponseDTO(comment));
    }

    @GetMapping("/products/{productId}/comments")
    public ResponseEntity<Page<CommentResponseDTO>> listProductComments(
            @PathVariable UUID productId,
            Pageable pageable) {
        Page<CommentResponseDTO> body = commentService.findByProduct(productId, pageable)
                .map(commentMapper::toResponseDTO);
        return ResponseEntity.ok(body);
    }

    @PostMapping("/products/{productId}/comments")
    public ResponseEntity<CommentResponseDTO> createProductComment(
            @PathVariable UUID productId,
            @Valid @RequestBody CommentRequestDTO commentRequestDTO,
            @AuthenticationPrincipal Jwt jwt) {
        UUID authorId = UUID.fromString(jwt.getSubject());
        Comment comment = commentService.createOnProduct(productId, commentRequestDTO, authorId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(commentMapper.toResponseDTO(comment));
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable UUID commentId,
            @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        commentService.delete(commentId, userId);
        return ResponseEntity.noContent().build();
    }
}
