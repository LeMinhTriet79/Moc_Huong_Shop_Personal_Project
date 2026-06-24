package com.minhtriet.se3979.notificationservice.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
public class OrderEventListener {

    // Lắng nghe chính xác cái topic mà Order Service vừa ném dữ liệu vào
    @KafkaListener(topics = "order.created", groupId = "notification-group")
    public void handleOrderCreated(Map<String, Object> orderData) {

        try {
            // Lấy mã đơn hàng
            String orderCode = (String) orderData.get("orderCode");

            // Ép kiểu an toàn: Ép sang Number trước, sau đó lấy giá trị Double
            Number amountObj = (Number) orderData.get("totalAmount");
            Double totalAmount = amountObj != null ? amountObj.doubleValue() : 0.0;

            log.info("==================================================");
            log.info("🔔 TING TING! NHẬN ĐƯỢC THÔNG BÁO TỪ KAFKA!");
            log.info("📧 Chuẩn bị gửi Email xác nhận cho đơn hàng: {}", orderCode);
            log.info("💰 Tổng tiền thu hộ (COD): {} VNĐ", totalAmount);
            log.info("==================================================");

            // Sau này ở đây bạn sẽ gọi EmailService.sendEmail(...) tích hợp SendGrid hoặc MailTrap
        } catch (Exception e) {
            log.error("Lỗi khi xử lý tin nhắn Kafka: {}", e.getMessage());
        }
    }
}