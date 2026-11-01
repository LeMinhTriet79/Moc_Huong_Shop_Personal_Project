package com.minhtriet.se3979.orderservice.kafka;

import com.minhtriet.se3979.orderservice.dto.event.OrderCancelledEvent;
import com.minhtriet.se3979.orderservice.dto.event.OrderCreatedEvent;
import com.minhtriet.se3979.orderservice.dto.event.OrderStatusChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void publishOrderCreated(OrderCreatedEvent event) {
        CompletableFuture.runAsync(() -> {
            try {
                String key = event.getOrderId() != null ? event.getOrderId().toString() : event.getOrderCode();
                kafkaTemplate.send("order.created", key, event);
                log.info("[KAFKA] Đã phát sự kiện order.created cho mã đơn: {}", event.getOrderCode());
            } catch (Exception e) {
                log.warn("[KAFKA-OFFLINE] Bỏ qua gửi sự kiện order.created cho đơn {}: {}", event.getOrderCode(), e.getMessage());
            }
        });
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void publishOrderCancelled(OrderCancelledEvent event) {
        CompletableFuture.runAsync(() -> {
            try {
                String key = event.getOrderId() != null ? event.getOrderId().toString() : event.getOrderCode();
                kafkaTemplate.send("order.cancelled", key, event);
                log.info("[KAFKA] Đã phát sự kiện order.cancelled cho mã đơn: {}", event.getOrderCode());
            } catch (Exception e) {
                log.warn("[KAFKA-OFFLINE] Bỏ qua gửi sự kiện order.cancelled cho đơn {}: {}", event.getOrderCode(), e.getMessage());
            }
        });
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void publishOrderStatusChanged(OrderStatusChangedEvent event) {
        CompletableFuture.runAsync(() -> {
            try {
                String key = event.getOrderId() != null ? event.getOrderId().toString() : event.getOrderCode();
                kafkaTemplate.send("order.status.changed", key, event);
                log.info("[KAFKA] Đã phát sự kiện order.status.changed cho mã đơn: {} -> {}",
                        event.getOrderCode(), event.getToStatus());
            } catch (Exception e) {
                log.warn("[KAFKA-OFFLINE] Bỏ qua gửi sự kiện order.status.changed cho đơn {}: {}", event.getOrderCode(), e.getMessage());
            }
        });
    }
}
