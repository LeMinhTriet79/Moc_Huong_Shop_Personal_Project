package com.minhtriet.se3979.catalogservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minhtriet.se3979.catalogservice.dto.request.CategoryRequest;
import com.minhtriet.se3979.catalogservice.dto.response.ApiResponse;
import com.minhtriet.se3979.catalogservice.dto.response.CategoryResponse;
import com.minhtriet.se3979.catalogservice.service.impl.CategoryServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/catalog/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryServiceImpl categoryService;
    private final ObjectMapper objectMapper;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAllCategories() {
        return ResponseEntity.ok(ApiResponse.success(
                categoryService.getAllActiveCategories(),
                "Lấy danh sách danh mục thành công"
        ));
    }

    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    @GetMapping("/inactive")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getInactiveCategories() {
        return ResponseEntity.ok(ApiResponse.success(
                categoryService.getAllInactiveCategories(),
                "Lấy danh sách danh mục đã ẩn thành công"
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> getCategoryById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                categoryService.getCategoryById(id),
                "Lấy chi tiết danh mục thành công"
        ));
    }

    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(
            @RequestPart("category") String categoryJson,
            @RequestPart(value = "image", required = false) MultipartFile file
    ) {
        try {
            CategoryRequest request = objectMapper.readValue(categoryJson, CategoryRequest.class);
            CategoryResponse response = categoryService.createCategory(request, file);
            return ResponseEntity.ok(ApiResponse.success(response, "Thêm danh mục mới thành công!"));
        } catch (Exception e) {
            throw new RuntimeException("Lỗi dữ liệu đầu vào: " + e.getMessage());
        }
    }

    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(
            @PathVariable Long id,
            @RequestPart(value = "category", required = false) String categoryJson,
            @RequestPart(value = "image", required = false) MultipartFile file
    ) {
        try {
            CategoryRequest request = null;
            if (categoryJson != null && !categoryJson.isEmpty()) {
                request = objectMapper.readValue(categoryJson, CategoryRequest.class);
            }
            CategoryResponse response = categoryService.updateCategory(id, request, file);
            return ResponseEntity.ok(ApiResponse.success(response, "Cập nhật danh mục thành công!"));
        } catch (Exception e) {
            throw new RuntimeException("Lỗi dữ liệu đầu vào: " + e.getMessage());
        }
    }

    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Đã đưa danh mục vào thùng rác thành công!"));
    }
}