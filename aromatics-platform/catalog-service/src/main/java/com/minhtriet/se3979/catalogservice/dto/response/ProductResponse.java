package com.minhtriet.se3979.catalogservice.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class ProductResponse {
    private Long id;
    private String name;
    private String slug;
    private String shortDescription;
    private String brand;
    private BigDecimal averageRating;
    private Integer totalReviews;
    private Integer totalSold;
    private String primaryImageUrl; // Chỉ lấy ảnh chính ra cho danh sách
    private BigDecimal startingPrice; // Lấy giá của variant rẻ nhất
}