package com.minhtriet.se3979.orderservice.redis;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {
    private Long productId;
    private Long variantId;
    private String productName;
    private String variantName;
    private String productImageUrl;
    private BigDecimal price; // Giá tại thời điểm thêm vào giỏ
    private int quantity;
}