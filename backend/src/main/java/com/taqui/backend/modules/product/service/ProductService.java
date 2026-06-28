package com.taqui.backend.modules.product.service;

import com.taqui.backend.modules.product.dto.ProductRequestDTO;
import com.taqui.backend.modules.product.entity.Product;
import com.taqui.backend.modules.product.exception.ProductNotFoundException;
import com.taqui.backend.modules.product.mapper.ProductMapper;
import com.taqui.backend.modules.product.repository.ProductRepository;
import com.taqui.backend.modules.user.entity.User;
import com.taqui.backend.modules.user.exception.UserNotFoundException;
import com.taqui.backend.modules.user.exception.WhatsappRequiredException;
import com.taqui.backend.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Page<Product> findAllProductsByOrderByCreatedAtDesc(String owner, Pageable pageable) {
        if (owner == null || owner.isBlank()) {
            return productRepository.findAllByOrderByCreatedAtDesc(pageable);
        }
        if (!userRepository.existsByUsername(owner)) {
            throw new UserNotFoundException("Usuário não encontrado: " + owner);
        }
        return productRepository.findByOwner_UsernameOrderByCreatedAtDesc(owner, pageable);
    }

    @Transactional(readOnly = true)
    public Product findProductById(UUID productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Produto não encontrado: " + productId));
    }

    @Transactional
    public Product createProduct(ProductRequestDTO productRequestDTO, UUID ownerId) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado"));
        if (owner.getWhatsapp() == null || owner.getWhatsapp().isBlank()) {
            throw new WhatsappRequiredException("Cadastre seu WhatsApp para vender");
        }
        Product product = productMapper.toEntity(productRequestDTO);
        product.setOwner(owner);
        return productRepository.save(product);
    }


    @Transactional
    public Product updateProduct(ProductRequestDTO productRequestDTO, UUID ownerId, UUID productId) {
        Product product = findProductById(productId);
        checkOwnership(product, ownerId);
        productMapper.updateEntityFromDTO(productRequestDTO, product);
        return product;
    }

    @Transactional
    public void delete(UUID productId, UUID ownerId) {
        Product product = findProductById(productId);
        checkOwnership(product, ownerId);
        productRepository.delete(product);
    }

    private void checkOwnership(Product product, UUID currentUserId) {
        if (!product.getOwner().getUserId().equals(currentUserId)) {
            throw new AccessDeniedException("Você não é o dono deste produto");
        }
    }
}
