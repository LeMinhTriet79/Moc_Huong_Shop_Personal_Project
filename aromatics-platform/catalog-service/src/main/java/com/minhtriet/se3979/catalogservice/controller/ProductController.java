package com.minhtriet.se3979.catalogservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minhtriet.se3979.catalogservice.dto.request.ProductCreateRequest;
import com.minhtriet.se3979.catalogservice.dto.request.ProductUpdateRequest;
import com.minhtriet.se3979.catalogservice.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/catalog/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final ObjectMapper objectMapper; // <--- CÔNG CỤ PARSE JSON THẦN THÁNH

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

    // BÙA CHÚ BẢO VỆ: Chỉ STAFF và ADMIN mới được gọi API này
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    @PostMapping(consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createProduct(
            @RequestPart("product") String productJson, // <--- HỨNG BẰNG STRING ĐỂ NÉ LỖI 415
            @RequestPart(value = "images", required = false) List<MultipartFile> files
    ) {
        try {
            // Tự tay dịch chuỗi String thành Object DTO
            ProductCreateRequest request = objectMapper.readValue(productJson, ProductCreateRequest.class);

            // Đẩy xuống Service xử lý như bình thường
            Object response = productService.createProductWithImages(request, files);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi định dạng JSON hoặc Upload: " + e.getMessage());
        }
    }

    // API Lấy Chi tiết Sản Phẩm (Dành cho tất cả mọi người)
    @GetMapping("/{slug}")
    public ResponseEntity<?> getProductDetail(@PathVariable String slug) {
        Object response = productService.getProductDetail(slug);
        return ResponseEntity.ok(response);
    }

    // CẬP NHẬT SẢN PHẨM (Sửa thông minh + Quản lý thư viện ảnh)
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    @PutMapping(value = "/{id}", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateProduct(
            @PathVariable Long id,
            @RequestPart(value = "product", required = false) String productJson,
            @RequestPart(value = "newImages", required = false) List<MultipartFile> newImages
    ) {
        try {
            ProductUpdateRequest request = null;
            if (productJson != null && !productJson.isEmpty()) {
                request = objectMapper.readValue(productJson, ProductUpdateRequest.class);
            }

            Object response = productService.updateProduct(id, request, newImages);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi dữ liệu đầu vào: " + e.getMessage());
        }
    }

}