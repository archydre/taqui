package com.taqui.backend.modules.cart.controller;

import com.taqui.backend.modules.cart.dto.AddToCartRequestDTO;
import com.taqui.backend.modules.cart.dto.CartItemResponseDTO;
import com.taqui.backend.modules.cart.dto.UpdateCartItemRequestDTO;
import com.taqui.backend.modules.cart.entity.CartItem;
import com.taqui.backend.modules.cart.mapper.CartMapper;
import com.taqui.backend.modules.cart.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final CartMapper cartMapper;

    @GetMapping
    public ResponseEntity<List<CartItemResponseDTO>> findMyCart(@AuthenticationPrincipal Jwt jwt) {
        UUID buyerId = UUID.fromString(jwt.getSubject());
        List<CartItemResponseDTO> body = cartService.findMyCart(buyerId).stream()
                .map(cartMapper::toResponseDTO)
                .toList();
        return ResponseEntity.ok(body);
    }

    @PostMapping("/items")
    public ResponseEntity<CartItemResponseDTO> addToCart(
            @Valid @RequestBody AddToCartRequestDTO dto,
            @AuthenticationPrincipal Jwt jwt) {
        UUID buyerId = UUID.fromString(jwt.getSubject());
        CartItem item = cartService.addToCart(buyerId, dto.productId(), dto.quantity());
        return ResponseEntity.ok(cartMapper.toResponseDTO(item));
    }

    @PatchMapping("/items/{productId}")
    public ResponseEntity<CartItemResponseDTO> updateItem(
            @PathVariable UUID productId,
            @Valid @RequestBody UpdateCartItemRequestDTO dto,
            @AuthenticationPrincipal Jwt jwt) {
        UUID buyerId = UUID.fromString(jwt.getSubject());
        CartItem item = cartService.updateQuantity(buyerId, productId, dto.quantity());
        return ResponseEntity.ok(cartMapper.toResponseDTO(item));
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<Void> removeItem(
            @PathVariable UUID productId,
            @AuthenticationPrincipal Jwt jwt) {
        UUID buyerId = UUID.fromString(jwt.getSubject());
        cartService.removeFromCart(buyerId, productId);
        return ResponseEntity.noContent().build();
    }
}
