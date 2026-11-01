package com.minhtriet.se3979.orderservice.enums;

import com.minhtriet.se3979.orderservice.exception.AppException;
import org.springframework.http.HttpStatus;

import java.util.Map;
import java.util.Set;

public enum OrderStatus {
    PENDING_PAYMENT,
    CONFIRMED,
    PREPARING,
    SHIPPING,
    DELIVERED,
    COMPLETED,
    CANCELLED,
    RETURN_REQUESTED,
    RETURNED;

    private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED_TRANSITIONS = Map.of(
            PENDING_PAYMENT, Set.of(CONFIRMED, CANCELLED),
            CONFIRMED, Set.of(PREPARING, CANCELLED),
            PREPARING, Set.of(SHIPPING, CANCELLED),
            SHIPPING, Set.of(DELIVERED),
            DELIVERED, Set.of(COMPLETED, RETURN_REQUESTED),
            RETURN_REQUESTED, Set.of(RETURNED, DELIVERED),
            COMPLETED, Set.of(),
            CANCELLED, Set.of(),
            RETURNED, Set.of()
    );

    public static OrderStatus fromString(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Trạng thái đơn hàng không được để trống");
        }
        String s = raw.trim().toUpperCase();
        return switch (s) {
            case "PENDING" -> PENDING_PAYMENT;
            case "PROCESSING" -> PREPARING;
            case "SHIPPED" -> SHIPPING;
            default -> {
                try {
                    yield OrderStatus.valueOf(s);
                } catch (IllegalArgumentException e) {
                    throw new AppException(HttpStatus.BAD_REQUEST, "Trạng thái đơn hàng không hợp lệ: " + raw);
                }
            }
        };
    }

    public boolean canTransitionTo(OrderStatus target) {
        if (this == target) return true;
        return ALLOWED_TRANSITIONS.getOrDefault(this, Set.of()).contains(target);
    }
}
