package com.taqui.backend.modules.cart.repository;

import com.taqui.backend.modules.cart.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CartItemRepository extends JpaRepository<CartItem, UUID> {

    List<CartItem> findByBuyer_UserIdOrderByCreatedAtDesc(UUID buyerId);

    Optional<CartItem> findByBuyer_UserIdAndProduct_ProductId(UUID buyerId, UUID productId);
}
