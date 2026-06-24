package com.minhtriet.se3979.catalogservice.service.impl;

import com.minhtriet.se3979.catalogservice.dto.response.ProductResponse;
import com.minhtriet.se3979.catalogservice.entity.Product;
import com.minhtriet.se3979.catalogservice.entity.ProductImage;
import com.minhtriet.se3979.catalogservice.entity.ProductVariant;
import com.minhtriet.se3979.catalogservice.repository.ProductRepository;
import com.minhtriet.se3979.catalogservice.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Override
    public Page<Object> searchProducts(String keyword, Long categoryId, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        Page<Product> products = productRepository.searchProducts(keyword, categoryId, minPrice, maxPrice, pageable);

        // Map Entity sang DTO
        return products.map(this::mapToResponse).map(dto -> (Object) dto);
    }

    @Override
    public Object getProductDetail(String slug) {
        // Sau này sẽ code thêm query chi tiết ở đây
        return null;
    }

    // Hàm tiện ích chuyển Entity sang DTO
    private ProductResponse mapToResponse(Product p) {
        String primaryImage = p.getImages().stream()
                .filter(ProductImage::getIsPrimary)
                .map(ProductImage::getImageUrl)
                .findFirst()
                .orElse(null);

        BigDecimal startPrice = p.getVariants().stream()
                .map(ProductVariant::getPrice)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        return ProductResponse.builder()
                .id(p.getId())
                .name(p.getName())
                .slug(p.getSlug())
                .shortDescription(p.getShortDescription())
                .brand(p.getBrand())
                .averageRating(p.getAverageRating())
                .totalReviews(p.getTotalReviews())
                .totalSold(p.getTotalSold())
                .primaryImageUrl(primaryImage)
                .startingPrice(startPrice)
                .build();
    }
}