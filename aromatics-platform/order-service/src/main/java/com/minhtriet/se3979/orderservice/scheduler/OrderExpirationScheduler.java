package com.minhtriet.se3979.orderservice.scheduler;

import com.minhtriet.se3979.orderservice.dto.event.OrderCancelledEvent;
import com.minhtriet.se3979.orderservice.dto.event.OrderItemEventDto;
import com.minhtriet.se3979.orderservice.dto.event.OrderStatusChangedEvent;
import com.minhtriet.se3979.orderservice.entity.Order;
import com.minhtriet.se3979.orderservice.entity.OrderStatusHistory;
import com.minhtriet.se3979.orderservice.kafka.OrderEventPublisher;
import com.minhtriet.se3979.orderservice.repository.OrderRepository;
import com.minhtriet.se3979.orderservice.repository.OrderStatusHistoryRepository;
import com.minhtriet.se3979.orderservice.service.CouponService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderExpirationScheduler {

    private final OrderRepository orderRepository;
    private final OrderStatusHistoryRepository historyRepository;
    private final CouponService couponService;
    private final OrderEventPublisher eventPublisher;

    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void cancelExpiredPendingPaymentOrders() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(15);
        List<Order> expiredOrders = orderRepository
                .findByStatusAndPaymentMethodAndCreatedAtBefore("PENDING_PAYMENT", "VNPAY", threshold);

        if (expiredOrders.isEmpty()) {
            return;
        }

        log.info("⏰ Phát hiện {} đơn hàng VNPay quá hạn 15 phút chưa thanh toán, tiến hành hủy tự động...",
                expiredOrders.size());

        for (Order order : expiredOrders) {
            String previousStatus = order.getStatus();
            order.setStatus("CANCELLED");
            order.setCancelledBy("SYSTEM");
            order.setCancelledReason("Quá hạn thanh toán VNPay (15 phút)");
            Order savedOrder = orderRepository.save(order);

            // Ghi lịch sử
            historyRepository.save(OrderStatusHistory.builder()
                    .order(savedOrder)
                    .fromStatus(previousStatus)
                    .toStatus("CANCELLED")
                    .changedByType("SYSTEM")
                    .changedById(0L)
                    .note("Hủy tự động do quá hạn thanh toán VNPay 15 phút")
                    .build());

            // Hoàn lượt dùng coupon
            couponService.refundCouponUsage(savedOrder.getId());

            // Phát sự kiện hủy đơn & hoàn kho
            List<OrderItemEventDto> eventItems = savedOrder.getItems() != null ? savedOrder.getItems().stream()
                    .map(item -> OrderItemEventDto.builder()
                            .variantId(item.getVariantId())
                            .quantity(item.getQuantity())
                            .sku(item.getSku())
                            .unitPrice(item.getUnitPrice())
                            .subtotal(item.getSubtotal())
                            .productName(item.getProductName())
                            .variantName(item.getVariantName())
                            .build())
                    .toList() : List.of();

            eventPublisher.publishOrderCancelled(OrderCancelledEvent.builder()
                    .orderId(savedOrder.getId())
                    .orderCode(savedOrder.getOrderCode())
                    .userId(savedOrder.getUserId())
                    .cancelledReason(savedOrder.getCancelledReason())
                    .cancelledBy("SYSTEM")
                    .items(eventItems)
                    .cancelledAt(LocalDateTime.now())
                    .build());

            eventPublisher.publishOrderStatusChanged(OrderStatusChangedEvent.builder()
                    .orderId(savedOrder.getId())
                    .orderCode(savedOrder.getOrderCode())
                    .userId(savedOrder.getUserId())
                    .fromStatus(previousStatus)
                    .toStatus("CANCELLED")
                    .note("Quá hạn thanh toán VNPay (15 phút)")
                    .changedAt(LocalDateTime.now())
                    .build());

            log.info("✅ Đã hủy đơn VNPay quá hạn: {}", savedOrder.getOrderCode());
        }
    }
}
