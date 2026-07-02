package com.taqui.backend.modules.product.service;

import com.taqui.backend.modules.audit.entity.AuditAction;
import com.taqui.backend.modules.audit.entity.AuditEntityType;
import com.taqui.backend.modules.audit.service.AuditService;
import com.taqui.backend.modules.product.dto.ProductRequestDTO;
import com.taqui.backend.modules.product.entity.Product;
import com.taqui.backend.modules.product.exception.ProductNotFoundException;
import com.taqui.backend.modules.product.mapper.ProductMapper;
import com.taqui.backend.modules.product.repository.ProductRepository;
import com.taqui.backend.modules.storage.service.StorageService;
import com.taqui.backend.modules.user.entity.User;
import com.taqui.backend.modules.user.exception.PixKeyRequiredException;
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
    private final StorageService storageService;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public Page<Product> findAllProductsByOrderByCreatedAtDesc(String q, String owner, Pageable pageable) {
        if (q != null && !q.isBlank()) {
            String term = q.trim();
            if (term.length() < 2) {
                return Page.empty(pageable);
            }
            return productRepository
                    .findByProductNameContainingIgnoreCaseOrProductDescriptionContainingIgnoreCaseOrderByCreatedAtDesc(
                            term, term, pageable);
        }
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
        if (owner.getPixKey() == null || owner.getPixKey().isBlank()) {
            throw new PixKeyRequiredException("Cadastre sua chave Pix para vender");
        }
        Product product = productMapper.toEntity(productRequestDTO);
        product.setOwner(owner);
        Product saved = productRepository.save(product);
        auditService.record(ownerId, AuditAction.CREATE, AuditEntityType.PRODUCT, saved.getProductId());
        return saved;
    }


    @Transactional
    public Product updateProduct(ProductRequestDTO productRequestDTO, UUID ownerId, UUID productId) {
        Product product = findProductById(productId);
        checkOwnership(product, ownerId);

        String oldImage = product.getImageUrl();
        String oldThumb = product.getThumbnailUrl();
        productMapper.updateEntityFromDTO(productRequestDTO, product);

        // Trocou por uma imagem nova e diferente? A antiga vira órfã no bucket -> apaga.
        String newImage = product.getImageUrl();
        if (newImage != null && !newImage.isBlank() && !newImage.equals(oldImage)) {
            storageService.deleteObject(oldImage);
            storageService.deleteObject(oldThumb);
        }
        auditService.record(ownerId, AuditAction.UPDATE, AuditEntityType.PRODUCT, productId);
        return product;
    }

    @Transactional
    public void delete(UUID productId, UUID ownerId) {
        Product product = findProductById(productId);
        checkOwnership(product, ownerId);
        auditService.record(ownerId, AuditAction.DELETE, AuditEntityType.PRODUCT, productId);
        storageService.deleteObject(product.getImageUrl());
        storageService.deleteObject(product.getThumbnailUrl());
        productRepository.delete(product);
    }

    private void checkOwnership(Product product, UUID currentUserId) {
        if (!product.getOwner().getUserId().equals(currentUserId)) {
            throw new AccessDeniedException("Você não é o dono deste produto");
        }
    }
}
