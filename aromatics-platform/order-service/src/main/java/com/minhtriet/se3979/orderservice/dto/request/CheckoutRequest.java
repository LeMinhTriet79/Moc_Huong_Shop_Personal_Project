package com.minhtriet.se3979.orderservice.dto.request;

import lombok.Data;
import java.util.Map;

@Data
public class CheckoutRequest {
    private Map<String, Object> shippingAddress; // Thông tin địa chỉ khách chọn
    private String paymentMethod; // VNPAY, MOMO, COD
    private String notes; // Lời nhắn
    private String idempotencyKey; // Sinh ngẫu nhiên từ Frontend để chống bấm đúp 2 lần
}