package com.minhtriet.se3979.catalogservice.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductCreateRequest {
    private Long categoryId;
    private String name;
    private String shortDescription;
    private String description;
    private String brand;
    private List<String> tags;

    // Hỗ trợ đa biến thể (Multi-variant)
    private List<VariantCreateRequest> variants;

    // Các trường fallback tạo variant mặc định
    @JsonAlias({"skuPrefix"})
    private String sku;

    @JsonAlias({"basePrice"})
    private BigDecimal price;

    private Integer stockQuantity;
    private Integer lowStockThreshold;
}
