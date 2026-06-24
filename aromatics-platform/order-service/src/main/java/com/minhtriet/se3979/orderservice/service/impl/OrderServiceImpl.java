package com.minhtriet.se3979.orderservice.service.impl;

import com.minhtriet.se3979.orderservice.dto.request.CheckoutRequest;
import com.minhtriet.se3979.orderservice.dto.request.DeductRequest;
import com.minhtriet.se3979.orderservice.entity.Order;
import com.minhtriet.se3979.orderservice.entity.OrderItem;
import com.minhtriet.se3979.orderservice.feign.CatalogFeignClient;
import com.minhtriet.se3979.orderservice.kafka.OrderEventPublisher;
import com.minhtriet.se3979.orderservice.redis.CartItem;
import com.minhtriet.se3979.orderservice.repository.OrderItemRepository;
import com.minhtriet.se3979.orderservice.repository.OrderRepository;
import com.minhtriet.se3979.orderservice.service.CartService;
import com.minhtriet.se3979.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService { // Nhớ implement OrderService interface nhé

    private final CartService cartService;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository; // Nhớ tạo 2 Repository này (extends JpaRepository)
    private final CatalogFeignClient catalogClient;
    private final OrderEventPublisher eventPublisher;

    @Transactional
    @Override
    public Order checkout(Long userId, CheckoutRequest request) {
        // 1. Lấy giỏ hàng từ Redis
        List<CartItem> cartItems = cartService.getCart(userId);
        if (cartItems.isEmpty()) {
            throw new RuntimeException("Giỏ hàng trống!");
        }

        // 2. Gọi chéo sang Catalog Service để trừ kho (Sync Feign Call)
        for (CartItem item : cartItems) {
            try {
                // Đây là nơi phép màu xảy ra: Gọi API nội bộ giữa các service!
                var response = catalogClient.deductInventory(new DeductRequest(item.getVariantId(), item.getQuantity()));
                if (!response.getStatusCode().is2xxSuccessful() || !response.getBody().isSuccess()) {
                    throw new RuntimeException("Lỗi trừ kho: " + response.getBody().getMessage());
                }
            } catch (Exception e) {
                // Nếu 1 món hết hàng, toàn bộ quá trình đặt hàng sẽ bị Rollback
                log.error("Hết hàng hoặc lỗi kết nối Catalog cho variant: {}", item.getVariantId(), e);
                throw new RuntimeException("Sản phẩm " + item.getProductName() + " đã hết hàng hoặc không đủ số lượng.");
            }
        }

        // 3. Tính toán tiền nong (Tạm thời giả lập phí ship 30k)
        BigDecimal subtotal = cartItems.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal shippingFee = BigDecimal.valueOf(30000);
        BigDecimal total = subtotal.add(shippingFee);

        // 4. Lưu Order vào MySQL
        Order order = Order.builder()
                .orderCode("MH-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .userId(userId)
                .shippingAddress(request.getShippingAddress())
                .paymentMethod(request.getPaymentMethod())
                .shippingFee(shippingFee)
                .subtotal(subtotal)
                .totalAmount(total)
                .idempotencyKey(request.getIdempotencyKey())
                .notes(request.getNotes())
                .build();
        Order savedOrder = orderRepository.save(order);

        // 5. Lưu chi tiết OrderItem
        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem c : cartItems) {
            OrderItem oi = OrderItem.builder()
                    .order(savedOrder)
                    .productId(c.getProductId())
                    .variantId(c.getVariantId())
                    .productName(c.getProductName())
                    .variantName(c.getVariantName())
                    .productImageUrl(c.getProductImageUrl())
                    .sku("SKU-" + c.getVariantId()) // Giả lập SKU
                    .unitPrice(c.getPrice())
                    .quantity(c.getQuantity())
                    .subtotal(c.getPrice().multiply(BigDecimal.valueOf(c.getQuantity())))
                    .build();
            orderItems.add(oi);
        }
        orderItemRepository.saveAll(orderItems);

        // 6. Xóa giỏ hàng trong Redis
        cartService.clearCart(userId);

        // 7. Gửi sự kiện Kafka báo tạo đơn thành công
        eventPublisher.publishOrderCreated(savedOrder);

        return savedOrder;
    }
}