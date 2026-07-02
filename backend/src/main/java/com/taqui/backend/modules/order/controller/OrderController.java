package com.taqui.backend.modules.order.controller;

import com.taqui.backend.modules.order.dto.OrderRequestDTO;
import com.taqui.backend.modules.order.dto.OrderResponseDTO;
import com.taqui.backend.modules.order.dto.PixQrResponseDTO;
import com.taqui.backend.modules.order.entity.Order;
import com.taqui.backend.modules.order.mapper.OrderMapper;
import com.taqui.backend.modules.order.service.OrderService;
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
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final OrderMapper orderMapper;

    @PostMapping
    public ResponseEntity<OrderResponseDTO> createOrder(
            @Valid @RequestBody OrderRequestDTO orderRequestDTO,
            @AuthenticationPrincipal Jwt jwt) {
        UUID buyerId = UUID.fromString(jwt.getSubject());
        Order order = orderService.createOrder(orderRequestDTO, buyerId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderMapper.toResponseDTO(order));
    }

    @GetMapping
    public ResponseEntity<Page<OrderResponseDTO>> findMyOrders(
            @AuthenticationPrincipal Jwt jwt,
            Pageable pageable) {
        UUID buyerId = UUID.fromString(jwt.getSubject());
        Page<OrderResponseDTO> body = orderService.findMyOrders(buyerId, pageable)
                .map(orderMapper::toResponseDTO);
        return ResponseEntity.ok(body);
    }

    @GetMapping("/received")
    public ResponseEntity<Page<OrderResponseDTO>> findReceivedOrders(
            @AuthenticationPrincipal Jwt jwt,
            Pageable pageable) {
        UUID sellerId = UUID.fromString(jwt.getSubject());
        Page<OrderResponseDTO> body = orderService.findReceivedOrders(sellerId, pageable)
                .map(orderMapper::toResponseDTO);
        return ResponseEntity.ok(body);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponseDTO> findOrderById(
            @PathVariable UUID orderId,
            @AuthenticationPrincipal Jwt jwt) {
        UUID currentUserId = UUID.fromString(jwt.getSubject());
        Order order = orderService.findOrderById(orderId, currentUserId);
        return ResponseEntity.ok(orderMapper.toResponseDTO(order));
    }

    @GetMapping("/{orderId}/pix-qr")
    public ResponseEntity<PixQrResponseDTO> getPixQr(
            @PathVariable UUID orderId,
            @AuthenticationPrincipal Jwt jwt) {
        UUID currentUserId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(orderService.getPixQr(orderId, currentUserId));
    }

    @PostMapping("/{orderId}/confirm-payment")
    public ResponseEntity<OrderResponseDTO> confirmPayment(
            @PathVariable UUID orderId,
            @AuthenticationPrincipal Jwt jwt) {
        UUID sellerId = UUID.fromString(jwt.getSubject());
        Order order = orderService.confirmPayment(orderId, sellerId);
        return ResponseEntity.ok(orderMapper.toResponseDTO(order));
    }

    @PostMapping("/{orderId}/ship")
    public ResponseEntity<OrderResponseDTO> shipOrder(
            @PathVariable UUID orderId,
            @AuthenticationPrincipal Jwt jwt) {
        UUID sellerId = UUID.fromString(jwt.getSubject());
        Order order = orderService.shipOrder(orderId, sellerId);
        return ResponseEntity.ok(orderMapper.toResponseDTO(order));
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponseDTO> cancelOrder(
            @PathVariable UUID orderId,
            @AuthenticationPrincipal Jwt jwt) {
        UUID currentUserId = UUID.fromString(jwt.getSubject());
        Order order = orderService.cancelOrder(orderId, currentUserId);
        return ResponseEntity.ok(orderMapper.toResponseDTO(order));
    }
}
