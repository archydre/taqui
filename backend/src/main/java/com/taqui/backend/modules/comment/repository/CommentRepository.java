package com.taqui.backend.modules.comment.repository;

import com.taqui.backend.modules.comment.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CommentRepository extends JpaRepository<Comment, UUID> {

    Page<Comment> findByPost_PostIdOrderByCreatedAtDesc(UUID postId, Pageable pageable);

    Page<Comment> findByProduct_ProductIdOrderByCreatedAtDesc(UUID productId, Pageable pageable);
}
