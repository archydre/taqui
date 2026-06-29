package com.taqui.backend.modules.order.dto;

import com.taqui.backend.modules.order.entity.OrderStatus;
import com.taqui.backend.modules.product.dto.ProductResponseDTO;
import com.taqui.backend.modules.user.dto.UserPublicInfoDTO;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record OrderResponseDTO(
        UUID orderId,
        UserPublicInfoDTO buyer,
        ProductResponseDTO product,
        Integer quantity,
        BigDecimal unitPrice,
        String freightService,
        BigDecimal freightPrice,
        BigDecimal total,
        String sellerPixKey,
        AddressDTO address,
        OrderStatus status,
        Instant createdAt,
        Instant updatedAt
) {}
