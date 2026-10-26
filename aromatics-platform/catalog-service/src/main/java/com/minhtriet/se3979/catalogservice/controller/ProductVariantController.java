package com.minhtriet.se3979.catalogservice.controller;

import com.minhtriet.se3979.catalogservice.dto.request.VariantCreateRequest;
import com.minhtriet.se3979.catalogservice.dto.request.VariantUpdateRequest;
import com.minhtriet.se3979.catalogservice.dto.response.ApiResponse;
import com.minhtriet.se3979.catalogservice.dto.response.ProductVariantResponse;
import com.minhtriet.se3979.catalogservice.dto.response.VariantInternalInfoResponse;
import com.minhtriet.se3979.catalogservice.service.ProductVariantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/catalog/products")
@RequiredArgsConstructor
public class ProductVariantController {

    private final ProductVariantService variantService;

    // Lấy danh sách biến thể của sản phẩm
    @GetMapping("/{productId}/variants")
    public ResponseEntity<ApiResponse<List<ProductVariantResponse>>> getVariantsByProductId(
            @PathVariable Long productId) {
        List<ProductVariantResponse> response = variantService.getVariantsByProductId(productId);
        return ResponseEntity.ok(ApiResponse.success(response, "Lấy danh sách biến thể thành công"));
    }

    // Thêm biến thể mới cho sản phẩm (STAFF, ADMIN)
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    @PostMapping("/{productId}/variants")
    public ResponseEntity<ApiResponse<ProductVariantResponse>> addVariant(
            @PathVariable Long productId,
            @Valid @RequestBody VariantCreateRequest request) {
        ProductVariantResponse response = variantService.addVariant(productId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Thêm biến thể sản phẩm thành công"));
    }

    // Cập nhật biến thể (STAFF, ADMIN)
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    @PutMapping("/{productId}/variants/{variantId}")
    public ResponseEntity<ApiResponse<ProductVariantResponse>> updateVariant(
            @PathVariable Long productId,
            @PathVariable Long variantId,
            @Valid @RequestBody VariantUpdateRequest request) {
        ProductVariantResponse response = variantService.updateVariant(productId, variantId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Cập nhật biến thể thành công"));
    }

    // Xóa mềm biến thể (STAFF, ADMIN)
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    @DeleteMapping("/{productId}/variants/{variantId}")
    public ResponseEntity<ApiResponse<String>> deleteVariant(
            @PathVariable Long productId,
            @PathVariable Long variantId) {
        variantService.deleteVariant(productId, variantId);
        return ResponseEntity.ok(ApiResponse.success("OK", "Đã ẩn biến thể thành công"));
    }

    // API nội bộ: Lấy thông tin biến thể cho Cart & Checkout (FeignClient)
    @GetMapping("/variants/{variantId}/internal")
    public ResponseEntity<ApiResponse<VariantInternalInfoResponse>> getVariantInternalInfo(
            @PathVariable Long variantId) {
        VariantInternalInfoResponse response = variantService.getVariantInternalInfo(variantId);
        return ResponseEntity.ok(ApiResponse.success(response, "Lấy thông tin biến thể nội bộ thành công"));
    }

    // API nội bộ: Lấy thông tin nhiều biến thể theo batch (FeignClient)
    @PostMapping("/variants/batch-info")
    public ResponseEntity<ApiResponse<List<VariantInternalInfoResponse>>> getBatchVariantInternalInfo(
            @RequestBody List<Long> variantIds) {
        List<VariantInternalInfoResponse> response = variantService.getBatchVariantInternalInfo(variantIds);
        return ResponseEntity.ok(ApiResponse.success(response, "Lấy danh sách thông tin biến thể nội bộ thành công"));
    }
}
