package com.taqui.backend.modules.product.repository;

import com.taqui.backend.modules.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    Page<Product> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<Product> findByOwner_UsernameOrderByCreatedAtDesc(String username, Pageable pageable);
}
