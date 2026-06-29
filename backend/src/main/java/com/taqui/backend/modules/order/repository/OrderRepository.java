package com.taqui.backend.modules.order.repository;

import com.taqui.backend.modules.order.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    Page<Order> findByBuyer_UserIdOrderByCreatedAtDesc(UUID buyerId, Pageable pageable);

    Page<Order> findByProduct_Owner_UserIdOrderByCreatedAtDesc(UUID sellerId, Pageable pageable);
}
