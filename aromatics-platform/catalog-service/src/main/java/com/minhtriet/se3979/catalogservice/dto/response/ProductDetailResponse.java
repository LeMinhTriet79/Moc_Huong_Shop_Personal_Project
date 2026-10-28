package com.minhtriet.se3979.catalogservice.dto.response;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDetailResponse {
    private Long id;
    private String name;
    private String slug;
    private String shortDescription;
    private String description;
    private String brand;
    private Long categoryId;
    private String categoryName;
    private List<String> tags;
    private BigDecimal averageRating;
    private Integer totalReviews;
    private Integer reviewCount;
    private Integer totalSold;
    private BigDecimal basePrice;
    private Boolean isActive;

    // Chứa danh sách toàn bộ ảnh
    private List<ImageDto> images;
    // Chứa danh sách các phiên bản (kèm số lượng tồn kho)
    private List<VariantDto> variants;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImageDto {
        private Long id;
        private String imageUrl;
        private Boolean isPrimary;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VariantDto {
        private Long id;
        private String sku;
        private String variantName;
        private BigDecimal price;
        private BigDecimal originalPrice;
        @JsonAlias({"weightGrams"})
        private Integer weightGram;
        private java.util.Map<String, Object> attributes;
        private Boolean isActive;
        private Integer stockQuantity;
        private Integer reservedQuantity;
        private Integer availableStock;

        public Integer getWeightGrams() {
            return weightGram;
        }
    }
}