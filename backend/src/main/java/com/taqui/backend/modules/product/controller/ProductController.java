package com.taqui.backend.modules.product.controller;

import com.taqui.backend.modules.product.dto.ProductRequestDTO;
import com.taqui.backend.modules.product.dto.ProductResponseDTO;
import com.taqui.backend.modules.product.entity.Product;
import com.taqui.backend.modules.product.mapper.ProductMapper;
import com.taqui.backend.modules.product.service.ProductService;
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
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final ProductMapper productMapper;

    @PostMapping
    public ResponseEntity<ProductResponseDTO> createProduct(
            @Valid @RequestBody ProductRequestDTO productRequestDTO,
            @AuthenticationPrincipal Jwt jwt) {
        UUID ownerId = UUID.fromString(jwt.getSubject());
        Product product = productService.createProduct(productRequestDTO, ownerId);
        return  ResponseEntity.status(HttpStatus.CREATED)
                .body(productMapper.toResponseDTO(product));
    }

    @GetMapping
    public ResponseEntity<Page<ProductResponseDTO>> findAllProducts(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String owner,
            Pageable pageable) {
        Page<ProductResponseDTO> body = productService.findAllProductsByOrderByCreatedAtDesc(q, owner, pageable)
                .map(productMapper::toResponseDTO);
        return ResponseEntity.ok(body);
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ProductResponseDTO> findProductById(@PathVariable UUID productId) {
        Product product = productService.findProductById(productId);
        return   ResponseEntity.ok(productMapper.toResponseDTO(product));
    }

    @PutMapping("/{productId}")
    public ResponseEntity<ProductResponseDTO> updateProduct(@PathVariable UUID productId,
                                                            @Valid @RequestBody ProductRequestDTO productRequestDTO,
                                                            @AuthenticationPrincipal Jwt jwt) {
        UUID currentUserId = UUID.fromString(jwt.getSubject());
        Product updatedProduct = productService.updateProduct(productRequestDTO, currentUserId, productId);
        return ResponseEntity.ok(productMapper.toResponseDTO(updatedProduct));
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteProduct(@PathVariable UUID productId,
                                              @AuthenticationPrincipal Jwt jwt) {
        UUID currentUserId = UUID.fromString(jwt.getSubject());
        productService.delete(productId, currentUserId);
        return ResponseEntity.noContent().build();
    }
}
