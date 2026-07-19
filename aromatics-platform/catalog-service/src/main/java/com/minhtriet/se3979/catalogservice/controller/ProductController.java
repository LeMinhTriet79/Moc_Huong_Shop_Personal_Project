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
    private final ObjectMapper objectMapper;

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

    // API: Lấy danh sách sản phẩm trong thùng rác
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    @GetMapping("/inactive")
    public ResponseEntity<Page<Object>> getInactiveProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {

        return ResponseEntity.ok(productService.getInactiveProducts(PageRequest.of(page, size)));
    }

    @GetMapping("/{slug}")
    public ResponseEntity<?> getProductDetail(@PathVariable String slug) {
        Object response = productService.getProductDetail(slug);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    @PostMapping(consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createProduct(
            @RequestPart("product") String productJson,
            @RequestPart(value = "images", required = false) List<MultipartFile> files
    ) {
        try {
            ProductCreateRequest request = objectMapper.readValue(productJson, ProductCreateRequest.class);
            Object response = productService.createProductWithImages(request, files);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi định dạng JSON hoặc Upload: " + e.getMessage());
        }
    }

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

    // API: Đưa sản phẩm vào thùng rác (Xóa mềm)
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok("Đã đưa sản phẩm vào thùng rác thành công!");
    }

    // API: Khôi phục sản phẩm từ thùng rác
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    @PutMapping("/{id}/restore")
    public ResponseEntity<?> restoreProduct(@PathVariable Long id) {
        productService.restoreProduct(id);
        return ResponseEntity.ok("Đã khôi phục sản phẩm thành công!");
    }

    // API: Xóa vĩnh viễn sản phẩm (Đổ rác)
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    @DeleteMapping("/{id}/force")
    public ResponseEntity<?> hardDeleteProduct(@PathVariable Long id) {
        productService.hardDeleteProduct(id);
        return ResponseEntity.ok("Đã xóa vĩnh viễn sản phẩm và dọn sạch ảnh trên mây!");
    }
}