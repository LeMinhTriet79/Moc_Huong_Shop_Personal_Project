package com.minhtriet.se3979.orderservice.kafka;

import com.minhtriet.se3979.orderservice.dto.event.OrderCancelledEvent;
import com.minhtriet.se3979.orderservice.dto.event.OrderCreatedEvent;
import com.minhtriet.se3979.orderservice.dto.event.OrderStatusChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishOrderCreated(OrderCreatedEvent event) {
        kafkaTemplate.send("order.created", event.getOrderId().toString(), event);
        log.info("[KAFKA] Đã phát sóng sự kiện tạo đơn hàng thành công cho Mã đơn: {}", event.getOrderCode());
    }

    public void publishOrderCancelled(OrderCancelledEvent event) {
        kafkaTemplate.send("order.cancelled", event.getOrderId().toString(), event);
        log.info("[KAFKA] Đã phát sóng sự kiện HỦY đơn hàng cho Mã đơn: {}", event.getOrderCode());
    }

    public void publishOrderStatusChanged(OrderStatusChangedEvent event) {
        kafkaTemplate.send("order.status.changed", event.getOrderId().toString(), event);
        log.info("[KAFKA] Đã phát sóng sự kiện đổi trạng thái đơn hàng {} sang {}", event.getOrderCode(), event.getToStatus());
    }
}