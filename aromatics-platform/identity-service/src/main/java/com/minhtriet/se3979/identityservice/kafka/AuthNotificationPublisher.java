package com.minhtriet.se3979.identityservice.kafka;

import com.minhtriet.se3979.identityservice.dto.event.AuthNotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthNotificationPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishAuthEvent(AuthNotificationEvent event) {
        // Gửi bất đồng bộ ở background thread để tuyệt đối không làm chậm response Đăng ký/Xác thực của khách hàng
        CompletableFuture.runAsync(() -> {
            try {
                kafkaTemplate.send("auth.notification", event.getUserId().toString(), event);
                log.info("[KAFKA] Đã phát sự kiện {} cho user {}: {}", event.getType(), event.getEmail(), event.getToken());
            } catch (Exception e) {
                log.warn("Không thể gửi sự kiện Kafka auth.notification (Kafka down hoặc chưa khởi động): {}", e.getMessage());
            }
        });
    }
}
