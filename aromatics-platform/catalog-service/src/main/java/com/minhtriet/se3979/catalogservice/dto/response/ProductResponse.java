package com.minhtriet.se3979.catalogservice.dto.response;

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
public class ProductResponse {
    private Long id;
    private String name;
    private String slug;
    private String shortDescription;
    private String description;
    private String brand;
    private Long categoryId;
    private String categoryName;
    private BigDecimal averageRating;
    private Integer totalReviews;
    private Integer reviewCount;
    private Integer totalSold;
    private String primaryImageUrl;
    private BigDecimal startingPrice;
    private BigDecimal basePrice;
    private Boolean isActive;
    private List<ProductDetailResponse.ImageDto> images;
    private List<ProductDetailResponse.VariantDto> variants;
}