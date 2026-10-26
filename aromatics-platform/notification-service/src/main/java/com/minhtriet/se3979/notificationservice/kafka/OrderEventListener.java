package com.minhtriet.se3979.notificationservice.kafka;

import com.minhtriet.se3979.notificationservice.service.NotificationDispatcherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderEventListener {

    private final NotificationDispatcherService dispatcherService;

    // 1. Lắng nghe sự kiện Tạo đơn hàng
    @KafkaListener(topics = "order.created", groupId = "notification-group")
    public void handleOrderCreated(Map<String, Object> orderData) {
        try {
            String orderCode = (String) orderData.get("orderCode");
            String userEmail = (String) orderData.get("userEmail");
            String fullName = (String) orderData.get("fullName");
            Number userIdNum = (Number) orderData.get("userId");
            Number orderIdNum = (Number) orderData.get("orderId");
            Number totalAmountNum = (Number) orderData.get("totalAmount");
            String paymentMethod = (String) orderData.get("paymentMethod");

            DecimalFormat df = new DecimalFormat("#,###");
            String formattedAmount = totalAmountNum != null ? df.format(totalAmountNum.doubleValue()) : "0";

            Map<String, Object> variables = new HashMap<>();
            variables.put("orderCode", orderCode);
            variables.put("fullName", fullName != null ? fullName : "Quý khách");
            variables.put("totalAmount", formattedAmount);
            variables.put("paymentMethod", paymentMethod != null ? paymentMethod : "COD");

            String idempotencyKey = "ORDER_CREATED_" + orderCode;

            dispatcherService.dispatchEmail(
                    idempotencyKey,
                    "ORDER_CREATED_EMAIL",
                    userEmail,
                    userIdNum != null ? userIdNum.longValue() : null,
                    "ORDER",
                    orderIdNum != null ? orderIdNum.longValue() : null,
                    variables
            );

        } catch (Exception e) {
            log.error("Lỗi khi xử lý Kafka order.created: {}", e.getMessage(), e);
        }
    }

    // 2. Lắng nghe sự kiện Hủy đơn hàng
    @KafkaListener(topics = "order.cancelled", groupId = "notification-group")
    public void handleOrderCancelled(Map<String, Object> orderData) {
        try {
            String orderCode = (String) orderData.get("orderCode");
            String userEmail = (String) orderData.get("userEmail");
            String reason = (String) orderData.get("cancelledReason");
            String cancelledBy = (String) orderData.get("cancelledBy");
            Number userIdNum = (Number) orderData.get("userId");
            Number orderIdNum = (Number) orderData.get("orderId");

            Map<String, Object> variables = new HashMap<>();
            variables.put("orderCode", orderCode);
            variables.put("cancelledReason", reason != null ? reason : "Không có lý do");
            variables.put("cancelledBy", cancelledBy != null ? cancelledBy : "Hệ thống");

            String idempotencyKey = "ORDER_CANCELLED_" + orderCode;

            dispatcherService.dispatchEmail(
                    idempotencyKey,
                    "ORDER_CANCELLED_EMAIL",
                    userEmail,
                    userIdNum != null ? userIdNum.longValue() : null,
                    "ORDER",
                    orderIdNum != null ? orderIdNum.longValue() : null,
                    variables
            );

        } catch (Exception e) {
            log.error("Lỗi khi xử lý Kafka order.cancelled: {}", e.getMessage(), e);
        }
    }

    // 3. Lắng nghe sự kiện Đổi trạng thái đơn hàng (CONFIRMED, SHIPPING, DELIVERED, v.v.)
    @KafkaListener(topics = "order.status.changed", groupId = "notification-group")
    public void handleOrderStatusChanged(Map<String, Object> orderData) {
        try {
            String orderCode = (String) orderData.get("orderCode");
            String userEmail = (String) orderData.get("userEmail");
            String fromStatus = (String) orderData.get("fromStatus");
            String toStatus = (String) orderData.get("toStatus");
            String note = (String) orderData.get("note");
            Number userIdNum = (Number) orderData.get("userId");
            Number orderIdNum = (Number) orderData.get("orderId");

            Map<String, Object> variables = new HashMap<>();
            variables.put("orderCode", orderCode);
            variables.put("fromStatus", fromStatus != null ? fromStatus : "");
            variables.put("toStatus", toStatus != null ? toStatus : "");
            variables.put("note", note != null ? note : "Đơn hàng đang được tiến hành xử lý");

            String idempotencyKey = "ORDER_STATUS_" + orderCode + "_" + toStatus;

            dispatcherService.dispatchEmail(
                    idempotencyKey,
                    "ORDER_STATUS_CHANGED_EMAIL",
                    userEmail,
                    userIdNum != null ? userIdNum.longValue() : null,
                    "ORDER",
                    orderIdNum != null ? orderIdNum.longValue() : null,
                    variables
            );

        } catch (Exception e) {
            log.error("Lỗi khi xử lý Kafka order.status.changed: {}", e.getMessage(), e);
        }
    }

    // 4. Lắng nghe sự kiện Xác thực Email & Quên mật khẩu từ identity-service
    @KafkaListener(topics = "auth.notification", groupId = "notification-group")
    public void handleAuthNotification(Map<String, Object> authData) {
        try {
            String type = (String) authData.get("type");
            String email = (String) authData.get("email");
            String fullName = (String) authData.get("fullName");
            String token = (String) authData.get("token");
            Number userIdNum = (Number) authData.get("userId");

            log.info("🔐 KAFKA: Nhận sự kiện auth.notification: type={}, email={}", type, email);

            Map<String, Object> variables = new HashMap<>();
            variables.put("fullName", fullName != null ? fullName : "Quý khách");
            variables.put("token", token != null ? token : "");

            if ("VERIFY_EMAIL".equalsIgnoreCase(type)) {
                String verificationLink = "http://localhost:8080/api/identity/auth/verify-email?token=" + token;
                variables.put("verificationLink", verificationLink);

                String idempotencyKey = "AUTH_VERIFY_" + email + "_" + token;
                dispatcherService.dispatchEmail(
                        idempotencyKey,
                        "VERIFY_EMAIL_TEMPLATE",
                        email,
                        userIdNum != null ? userIdNum.longValue() : null,
                        "USER",
                        userIdNum != null ? userIdNum.longValue() : null,
                        variables
                );
            } else if ("RESET_PASSWORD".equalsIgnoreCase(type)) {
                String resetLink = "http://localhost:3000/reset-password?token=" + token;
                variables.put("resetLink", resetLink);

                String idempotencyKey = "AUTH_RESET_" + email + "_" + token;
                dispatcherService.dispatchEmail(
                        idempotencyKey,
                        "RESET_PASSWORD_TEMPLATE",
                        email,
                        userIdNum != null ? userIdNum.longValue() : null,
                        "USER",
                        userIdNum != null ? userIdNum.longValue() : null,
                        variables
                );
            }

        } catch (Exception e) {
            log.error("Lỗi khi xử lý Kafka auth.notification: {}", e.getMessage(), e);
        }
    }
}