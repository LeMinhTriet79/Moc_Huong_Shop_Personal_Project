package com.minhtriet.se3979.orderservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VariantInternalInfoResponse {
    private Long productId;
    private Long variantId;
    private String productName;
    private String variantName;
    private String sku;
    private BigDecimal price;
    private String primaryImageUrl;
    private Integer availableStock;
    private Boolean isActive;
}
