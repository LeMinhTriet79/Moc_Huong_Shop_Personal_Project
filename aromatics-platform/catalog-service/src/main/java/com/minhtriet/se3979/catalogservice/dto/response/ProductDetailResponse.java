package com.minhtriet.se3979.catalogservice.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class ProductDetailResponse {
    private Long id;
    private String name;
    private String slug;
    private String shortDescription;
    private String description;
    private String brand;
    private List<String> tags;
    private BigDecimal averageRating;
    private Integer totalReviews;
    private Integer totalSold;
    private Boolean isActive;

    // Chứa danh sách toàn bộ ảnh
    private List<ImageDto> images;
    // Chứa danh sách các phiên bản (kèm số lượng tồn kho)
    private List<VariantDto> variants;

    @Data
    @Builder
    public static class ImageDto {
        private Long id;
        private String imageUrl;
        private Boolean isPrimary;
    }

    @Data
    @Builder
    public static class VariantDto {
        private Long id;
        private String sku;
        private String variantName;
        private BigDecimal price;
        private Integer stockQuantity;
    }
}