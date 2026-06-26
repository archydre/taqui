package com.taqui.backend.modules.post.controller;

import com.taqui.backend.modules.post.dto.PostRequestDTO;
import com.taqui.backend.modules.post.dto.PostResponseDTO;
import com.taqui.backend.modules.post.entity.Post;
import com.taqui.backend.modules.post.mapper.PostMapper;
import com.taqui.backend.modules.post.service.PostService;
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
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final PostMapper postMapper;

    @PostMapping
    public ResponseEntity<PostResponseDTO> createPost(
            @Valid @RequestBody PostRequestDTO postRequestDTO,
            @AuthenticationPrincipal Jwt jwt) {
        UUID ownerId = UUID.fromString(jwt.getSubject());
        Post post = postService.createPost(postRequestDTO, ownerId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(postMapper.toResponseDTO(post));
    }

    @GetMapping
    public ResponseEntity<Page<PostResponseDTO>> findAllPosts(Pageable pageable) {
        Page<PostResponseDTO> body = postService.findAllByOrderByCreatedAtDesc(pageable)
                .map(postMapper::toResponseDTO);
        return ResponseEntity.ok(body);
    }

    @GetMapping("/{postId}")
    public ResponseEntity<PostResponseDTO> findPostById(@PathVariable UUID postId) {
        Post post = postService.findPostById(postId);
        return ResponseEntity.ok(postMapper.toResponseDTO(post));
    }

    @PutMapping("/{postId}")
    public ResponseEntity<PostResponseDTO> updatePost(
            @PathVariable UUID postId,
            @Valid @RequestBody PostRequestDTO postRequestDTO,
            @AuthenticationPrincipal Jwt jwt) {
        UUID ownerId = UUID.fromString(jwt.getSubject());
        Post post = postService.updatePost(postRequestDTO, ownerId, postId);
        return ResponseEntity.ok(postMapper.toResponseDTO(post));
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(
            @PathVariable UUID postId,
            @AuthenticationPrincipal Jwt jwt) {
        UUID ownerId = UUID.fromString(jwt.getSubject());
        postService.deletePost(postId, ownerId);
        return ResponseEntity.noContent().build();
    }
}
