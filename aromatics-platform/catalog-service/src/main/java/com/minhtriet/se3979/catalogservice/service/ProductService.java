package com.minhtriet.se3979.catalogservice.service;

import com.minhtriet.se3979.catalogservice.dto.request.ProductCreateRequest;
import com.minhtriet.se3979.catalogservice.dto.request.ProductUpdateRequest;
import com.minhtriet.se3979.catalogservice.dto.response.ProductDetailResponse;
import com.minhtriet.se3979.catalogservice.dto.response.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

public interface ProductService {

    Page<ProductResponse> searchProducts(String keyword, Long categoryId, String brand, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

    Page<ProductResponse> getInactiveProducts(Pageable pageable);

    List<ProductResponse> getFeaturedProducts();

    ProductDetailResponse getProductDetail(String slug);

    ProductDetailResponse createProductWithImages(ProductCreateRequest request, List<MultipartFile> files);

    ProductDetailResponse updateProduct(Long productId, ProductUpdateRequest request, List<MultipartFile> newImages);

    void deleteProduct(Long productId);

    void restoreProduct(Long productId);

    void hardDeleteProduct(Long productId);
}