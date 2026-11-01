package com.minhtriet.se3979.orderservice.dto.response;

import com.minhtriet.se3979.orderservice.entity.OrderItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemResponse {
    private Long id;
    private Long productId;
    private Long variantId;
    private String productName;
    private String variantName;
    private String productImageUrl;
    private String sku;
    private BigDecimal unitPrice;
    private Integer quantity;
    private BigDecimal subtotal;
    private BigDecimal totalPrice;
    private Long reviewId;

    public BigDecimal getTotalPrice() {
        if (totalPrice != null) {
            return totalPrice;
        }
        if (subtotal != null) {
            return subtotal;
        }
        if (unitPrice != null && quantity != null) {
            return unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
        return BigDecimal.ZERO;
    }

    public static OrderItemResponse fromEntity(OrderItem item) {
        if (item == null) return null;
        return OrderItemResponse.builder()
                .id(item.getId())
                .productId(item.getProductId())
                .variantId(item.getVariantId())
                .productName(item.getProductName())
                .variantName(item.getVariantName())
                .productImageUrl(item.getProductImageUrl())
                .sku(item.getSku())
                .unitPrice(item.getUnitPrice())
                .quantity(item.getQuantity())
                .subtotal(item.getSubtotal())
                .totalPrice(item.getSubtotal())
                .reviewId(item.getReviewId())
                .build();
    }
}
