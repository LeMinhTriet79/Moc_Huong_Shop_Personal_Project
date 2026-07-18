package com.minhtriet.se3979.catalogservice.dto.request;

import lombok.Data;
import java.util.List;

@Data
public class ProductCreateRequest {
    private Long categoryId;
    private String name;
    private String shortDescription;
    private String description;
    private String brand;
    private List<String> tags;
    // Tạm thời làm sản phẩm đơn giản, Variant tính sau nhé!
}