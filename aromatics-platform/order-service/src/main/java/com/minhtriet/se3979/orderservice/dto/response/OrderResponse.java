package com.minhtriet.se3979.orderservice.dto.response;

import com.minhtriet.se3979.orderservice.entity.Order;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private Long id;
    private String orderCode;
    private Long userId;
    private String status;
    private Map<String, Object> shippingAddress;
    private BigDecimal shippingFee;
    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    private String couponCode;
    private String paymentMethod;
    private String paymentStatus;
    private String notes;
    private String staffNotes;
    private String trackingNumber;
    private String carrier;
    private String cancelledReason;
    private String cancelledBy;
    private LocalDateTime confirmedAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<OrderItemResponse> items;

    public static Map<String, Object> normalizeShippingAddress(Map<String, Object> raw) {
        if (raw == null || raw.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, Object> normalized = new LinkedHashMap<>(raw);
        Object recipientName = firstNonNull(raw, "recipientName", "recipient_name", "fullName");
        Object phone = firstNonNull(raw, "phoneNumber", "recipientPhone", "recipient_phone", "phone");
        Object street = firstNonNull(raw, "streetAddress", "street_address", "address");
        Object ward = firstNonNull(raw, "ward", "wardName", "ward_name");
        Object district = firstNonNull(raw, "district", "districtName", "district_name");
        Object province = firstNonNull(raw, "province", "provinceName", "province_name");

        if (recipientName != null) {
            normalized.put("recipientName", recipientName);
            normalized.put("recipient_name", recipientName);
        }
        if (phone != null) {
            normalized.put("phoneNumber", phone);
            normalized.put("recipientPhone", phone);
            normalized.put("recipient_phone", phone);
        }
        if (street != null) {
            normalized.put("streetAddress", street);
            normalized.put("street_address", street);
        }
        if (ward != null) {
            normalized.put("ward", ward);
            normalized.put("wardName", ward);
            normalized.put("ward_name", ward);
        }
        if (district != null) {
            normalized.put("district", district);
            normalized.put("districtName", district);
            normalized.put("district_name", district);
        }
        if (province != null) {
            normalized.put("province", province);
            normalized.put("provinceName", province);
            normalized.put("province_name", province);
        }
        return normalized;
    }

    private static Object firstNonNull(Map<String, Object> map, String... keys) {
        for (String key : keys) {
            Object val = map.get(key);
            if (val != null && !val.toString().isBlank()) {
                return val;
            }
        }
        return null;
    }

    public static OrderResponse fromEntity(Order order) {
        if (order == null) return null;
        List<OrderItemResponse> itemResponses = order.getItems() != null
                ? order.getItems().stream().map(OrderItemResponse::fromEntity).toList()
                : Collections.emptyList();

        return OrderResponse.builder()
                .id(order.getId())
                .orderCode(order.getOrderCode())
                .userId(order.getUserId())
                .status(order.getStatus())
                .shippingAddress(normalizeShippingAddress(order.getShippingAddress()))
                .shippingFee(order.getShippingFee())
                .subtotal(order.getSubtotal())
                .discountAmount(order.getDiscountAmount())
                .totalAmount(order.getTotalAmount())
                .couponCode(order.getCouponCode())
                .paymentMethod(order.getPaymentMethod())
                .paymentStatus(order.getPaymentStatus())
                .notes(order.getNotes())
                .staffNotes(order.getStaffNotes())
                .trackingNumber(order.getTrackingNumber())
                .carrier(order.getCarrier())
                .cancelledReason(order.getCancelledReason())
                .cancelledBy(order.getCancelledBy())
                .confirmedAt(order.getConfirmedAt())
                .deliveredAt(order.getDeliveredAt())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .items(itemResponses)
                .build();
    }
}
