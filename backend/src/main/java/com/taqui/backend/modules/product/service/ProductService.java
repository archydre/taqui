package com.taqui.backend.modules.product.service;

import com.taqui.backend.modules.product.dto.ProductRequestDTO;
import com.taqui.backend.modules.product.entity.Product;
import com.taqui.backend.modules.product.exception.ProductNotFoundException;
import com.taqui.backend.modules.product.mapper.ProductMapper;
import com.taqui.backend.modules.product.repository.ProductRepository;
import com.taqui.backend.modules.user.entity.User;
import com.taqui.backend.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final UserRepository userRepository;

    @Transactional
    public Product create(ProductRequestDTO dto, UUID ownerId) {
        Product product = productMapper.toEntity(dto);   // owner fica null aqui (foi ignorado no mapper)
        User owner = userRepository.getReferenceById(ownerId);
        product.setOwner(owner);                         // ← o dono entra AQUI, vindo do JWT
        return productRepository.save(product);
    }

    @Transactional(readOnly = true)
    public List<Product> findAll() {
        return productRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Product findById(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Produto não encontrado: " + id));
    }

    @Transactional
    public Product update(UUID id, ProductRequestDTO dto, UUID currentUserId) {
        Product product = findById(id);             // 404 se não existir
        checkOwnership(product, currentUserId);     // 403 se não for o dono

        product.setProductName(dto.productName());
        product.setProductDescription(dto.productDescription());
        product.setPrice(dto.price());
        product.setImageUrl(dto.imageUrl());
        return product;     // não precisa save() — ver "dirty checking" abaixo
    }

    @Transactional
    public void delete(UUID id, UUID currentUserId) {
        Product product = findById(id);
        checkOwnership(product, currentUserId);
        productRepository.delete(product);
    }

    private void checkOwnership(Product product, UUID currentUserId) {
        if (!product.getOwner().getUserId().equals(currentUserId)) {
            throw new AccessDeniedException("Você não é o dono deste produto");
        }
    }
}
