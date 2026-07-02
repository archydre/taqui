package com.taqui.backend.modules.cart.service;

import com.taqui.backend.modules.cart.entity.CartItem;
import com.taqui.backend.modules.cart.exception.CartItemNotFoundException;
import com.taqui.backend.modules.cart.repository.CartItemRepository;
import com.taqui.backend.modules.order.exception.SelfPurchaseException;
import com.taqui.backend.modules.product.entity.Product;
import com.taqui.backend.modules.product.service.ProductService;
import com.taqui.backend.modules.user.entity.User;
import com.taqui.backend.modules.user.exception.UserNotFoundException;
import com.taqui.backend.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ProductService productService;

    @Transactional(readOnly = true)
    public List<CartItem> findMyCart(UUID buyerId) {
        return cartItemRepository.findByBuyer_UserIdOrderByCreatedAtDesc(buyerId);
    }

    @Transactional
    public CartItem addToCart(UUID buyerId, UUID productId, int quantity) {
        User buyer = userRepository.findById(buyerId)
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado"));
        Product product = productService.findProductById(productId);

        if (product.getOwner().getUserId().equals(buyerId)) {
            throw new SelfPurchaseException("Você não pode adicionar seu próprio produto ao carrinho");
        }

        // (buyer, product) é único: se o item já existe, soma a quantidade em vez de duplicar a linha.
        return cartItemRepository.findByBuyer_UserIdAndProduct_ProductId(buyerId, productId)
                .map(item -> {
                    item.setQuantity(item.getQuantity() + quantity);
                    return cartItemRepository.save(item);
                })
                .orElseGet(() -> {
                    CartItem item = new CartItem();
                    item.setBuyer(buyer);
                    item.setProduct(product);
                    item.setQuantity(quantity);
                    return cartItemRepository.save(item);
                });
    }

    @Transactional
    public CartItem updateQuantity(UUID buyerId, UUID productId, int quantity) {
        CartItem item = cartItemRepository.findByBuyer_UserIdAndProduct_ProductId(buyerId, productId)
                .orElseThrow(() -> new CartItemNotFoundException("Item não está no carrinho"));
        item.setQuantity(quantity);
        return cartItemRepository.save(item);
    }

    @Transactional
    public void removeFromCart(UUID buyerId, UUID productId) {
        CartItem item = cartItemRepository.findByBuyer_UserIdAndProduct_ProductId(buyerId, productId)
                .orElseThrow(() -> new CartItemNotFoundException("Item não está no carrinho"));
        cartItemRepository.delete(item);
    }
}
