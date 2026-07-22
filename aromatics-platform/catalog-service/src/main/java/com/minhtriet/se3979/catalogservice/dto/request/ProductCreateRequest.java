package com.minhtriet.se3979.catalogservice.dto.request;

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

    // BỔ SUNG 3 TRƯỜNG NÀY ĐỂ TẠO VARIANT VÀ TỒN KHO MẶC ĐỊNH
    private String sku;
    private BigDecimal price;
    private Integer stockQuantity;
}