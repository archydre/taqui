package com.taqui.backend.modules.order.service;

import com.taqui.backend.modules.order.dto.OrderRequestDTO;
import com.taqui.backend.modules.order.dto.PixQrResponseDTO;
import com.taqui.backend.modules.order.entity.Order;
import com.taqui.backend.modules.order.entity.OrderStatus;
import com.taqui.backend.modules.order.exception.InvalidOrderStateException;
import com.taqui.backend.modules.order.exception.OrderNotFoundException;
import com.taqui.backend.modules.order.exception.SelfPurchaseException;
import com.taqui.backend.modules.order.mapper.OrderMapper;
import com.taqui.backend.modules.order.repository.OrderRepository;
import com.taqui.backend.modules.product.entity.Product;
import com.taqui.backend.modules.product.service.ProductService;
import com.taqui.backend.modules.user.entity.User;
import com.taqui.backend.modules.user.exception.PixKeyRequiredException;
import com.taqui.backend.modules.user.exception.UserNotFoundException;
import com.taqui.backend.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final UserRepository userRepository;
    private final ProductService productService;

    @Transactional
    public Order createOrder(OrderRequestDTO dto, UUID buyerId) {
        User buyer = userRepository.findById(buyerId)
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado"));
        Product product = productService.findProductById(dto.productId());
        User seller = product.getOwner();

        if (seller.getUserId().equals(buyerId)) {
            throw new SelfPurchaseException("Você não pode comprar seu próprio produto");
        }

        if (seller.getPixKey() == null || seller.getPixKey().isBlank()) {
            throw new PixKeyRequiredException("O vendedor ainda não configurou o Pix");
        }

        BigDecimal unitPrice = product.getPrice();
        BigDecimal total = unitPrice
                .multiply(BigDecimal.valueOf(dto.quantity()))
                .add(dto.freightPrice());

        Order order = new Order();
        order.setBuyer(buyer);
        order.setProduct(product);
        order.setQuantity(dto.quantity());
        order.setUnitPrice(unitPrice);
        order.setFreightService(dto.freightService());
        order.setFreightPrice(dto.freightPrice());
        order.setTotal(total);
        order.setSellerPixKey(seller.getPixKey());
        order.setAddress(orderMapper.toAddress(dto.address()));
        order.setStatus(OrderStatus.AGUARDANDO_PAGAMENTO);
        return orderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public Page<Order> findMyOrders(UUID buyerId, Pageable pageable) {
        return orderRepository.findByBuyer_UserIdOrderByCreatedAtDesc(buyerId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Order> findReceivedOrders(UUID sellerId, Pageable pageable) {
        return orderRepository.findByProduct_Owner_UserIdOrderByCreatedAtDesc(sellerId, pageable);
    }

    @Transactional(readOnly = true)
    public Order findOrderById(UUID orderId, UUID currentUserId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Pedido não encontrado: " + orderId));
        checkParticipant(order, currentUserId);
        return order;
    }

    // O vendedor não tem cidade cadastrada; usa um placeholder no BR Code (campo obrigatório).
    private static final String MERCHANT_CITY = "BRASIL";

    @Transactional(readOnly = true)
    public PixQrResponseDTO getPixQr(UUID orderId, UUID currentUserId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Pedido não encontrado: " + orderId));
        checkParticipant(order, currentUserId);
        String copyPaste = PixBrCodeGenerator.build(
                order.getSellerPixKey(),
                order.getTotal(),
                order.getProduct().getOwner().getDisplayName(),
                MERCHANT_CITY,
                order.getOrderId().toString());
        return new PixQrResponseDTO(copyPaste, order.getTotal());
    }

    @Transactional
    public Order confirmPayment(UUID orderId, UUID sellerId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Pedido não encontrado: " + orderId));
        if (!order.getProduct().getOwner().getUserId().equals(sellerId)) {
            throw new AccessDeniedException("Apenas o vendedor confirma o pagamento");
        }
        if (order.getStatus() != OrderStatus.AGUARDANDO_PAGAMENTO) {
            throw new InvalidOrderStateException("Pedido não está aguardando pagamento");
        }
        order.setStatus(OrderStatus.PAGO);
        return order;
    }

    @Transactional
    public Order shipOrder(UUID orderId, UUID sellerId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Pedido não encontrado: " + orderId));
        if (!order.getProduct().getOwner().getUserId().equals(sellerId)) {
            throw new AccessDeniedException("Apenas o vendedor pode enviar o pedido");
        }
        if (order.getStatus() != OrderStatus.PAGO) {
            throw new InvalidOrderStateException("Só dá pra enviar um pedido pago");
        }
        order.setStatus(OrderStatus.ENVIADO);
        return order;
    }

    @Transactional
    public Order cancelOrder(UUID orderId, UUID currentUserId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Pedido não encontrado: " + orderId));
        checkParticipant(order, currentUserId);
        if (order.getStatus() == OrderStatus.ENVIADO || order.getStatus() == OrderStatus.CANCELADO) {
            throw new InvalidOrderStateException("Pedido não pode mais ser cancelado");
        }
        order.setStatus(OrderStatus.CANCELADO);
        return order;
    }

    private void checkParticipant(Order order, UUID userId) {
        boolean isBuyer = order.getBuyer().getUserId().equals(userId);
        boolean isSeller = order.getProduct().getOwner().getUserId().equals(userId);
        if (!isBuyer && !isSeller) {
            throw new AccessDeniedException("Você não participa deste pedido");
        }
    }
}
