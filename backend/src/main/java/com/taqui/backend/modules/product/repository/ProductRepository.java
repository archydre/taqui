package com.taqui.backend.modules.product.repository;

import com.taqui.backend.modules.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {
}
