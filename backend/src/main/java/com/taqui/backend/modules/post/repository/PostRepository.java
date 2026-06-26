package com.taqui.backend.modules.post.repository;

import com.taqui.backend.modules.post.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PostRepository extends JpaRepository<Post, UUID> {

    Page<Post> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
