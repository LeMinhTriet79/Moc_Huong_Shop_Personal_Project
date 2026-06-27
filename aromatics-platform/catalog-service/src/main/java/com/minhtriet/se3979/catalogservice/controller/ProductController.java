package com.minhtriet.se3979.catalogservice.controller;

import com.minhtriet.se3979.catalogservice.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/catalog/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    // API: GET /api/catalog/products?keyword=nhang&page=0&size=12
    @GetMapping
    public ResponseEntity<Page<Object>> searchProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {

        return ResponseEntity.ok(productService.searchProducts(keyword, categoryId, minPrice, maxPrice, PageRequest.of(page, size)));
    }


    @GetMapping ("/test")// API tạo sản phẩm mới
    @PreAuthorize("hasRole('ADMIN')") // <--- CHỈ ADMIN MỚI ĐƯỢC GỌI HÀM NÀY
    public ResponseEntity<?> createProduct() {
        // Code tạo sản phẩm của bạn ở đây...
        return ResponseEntity.ok("Tạo sản phẩm thành công!");
    }
}