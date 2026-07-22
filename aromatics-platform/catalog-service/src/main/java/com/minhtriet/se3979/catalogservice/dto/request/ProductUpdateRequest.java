package com.minhtriet.se3979.catalogservice.dto.request;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductUpdateRequest {
    private String name;
    private Long categoryId;
    private String shortDescription;
    private String description;
    private String brand;
    private Boolean isPublished;

    // THÔNG TIN BIẾN THỂ MẶC ĐỊNH (Variant)
    private BigDecimal price;
    private Integer quantity;

    // QUAN TRỌNG NHẤT: Danh sách các ID của ảnh cũ mà Admin muốn XÓA
    private List<Long> deletedImageIds;
}