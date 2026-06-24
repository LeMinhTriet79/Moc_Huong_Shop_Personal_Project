package com.minhtriet.se3979.orderservice.kafka;

import com.minhtriet.se3979.orderservice.entity.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishOrderCreated(Order order) {
        // Gửi thông tin đơn hàng vào topic "order.created"
        // Trong dự án thực tế nên bọc Order lại thành OrderEventDTO, ở đây ta dùng Entity luôn cho nhanh
        kafkaTemplate.send("order.created", order.getId().toString(), order);
        log.info("[KAFKA] Đã phát sóng sự kiện tạo đơn hàng thành công cho Mã đơn: {}", order.getOrderCode());
    }
    public void publishOrderCancelled(Order order) {
        // Gửi thông tin đơn hàng bị hủy vào topic "order.cancelled"
        kafkaTemplate.send("order.cancelled", order.getId().toString(), order);
        log.info("[KAFKA] Đã phát sóng sự kiện HỦY đơn hàng cho Mã đơn: {}", order.getOrderCode());
    }
}