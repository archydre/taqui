package com.taqui.backend.modules.post.repository;

import com.taqui.backend.modules.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PostRepository extends JpaRepository<Post, UUID> {
}
