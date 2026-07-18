package com.minhtriet.se3979.catalogservice.dto.request;

import lombok.Data;

@Data
public class CategoryRequest {
    private String name;
    private Long parentId; // Có thể null nếu là danh mục gốc

    private String description;
    private Integer sortOrder;
    private Boolean isActive;
}