package com.minhtriet.se3979.catalogservice.service;

import com.minhtriet.se3979.catalogservice.dto.request.ProductCreateRequest;
import com.minhtriet.se3979.catalogservice.dto.request.ProductUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

public interface ProductService {
    Page<Object> searchProducts(String keyword, Long categoryId, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);
    Object getProductDetail(String slug);
    Object createProductWithImages(ProductCreateRequest request, List<MultipartFile> files);
    void deleteProduct(Long productId);
    Object updateProduct(Long productId, ProductUpdateRequest request, List<MultipartFile> newImages);
    Page<Object> getInactiveProducts(Pageable pageable); // Lấy danh sách thùng rác
    void restoreProduct(Long productId);
    void hardDeleteProduct(Long productId);
}