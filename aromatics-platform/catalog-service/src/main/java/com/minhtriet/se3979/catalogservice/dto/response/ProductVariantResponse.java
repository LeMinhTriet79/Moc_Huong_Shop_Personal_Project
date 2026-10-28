package com.minhtriet.se3979.catalogservice.dto.response;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariantResponse {
    private Long id;
    private Long productId;
    private String sku;
    private String variantName;
    private BigDecimal price;
    private BigDecimal originalPrice;
    @JsonAlias({"weightGrams"})
    private Integer weightGram;

    public Integer getWeightGrams() {
        return weightGram;
    }

    private Map<String, Object> attributes;
    private Boolean isActive;
    private Integer sortOrder;
    private Integer stockQuantity;
    private Integer reservedQuantity;
    private Integer availableStock;
    private LocalDateTime createdAt;
}
