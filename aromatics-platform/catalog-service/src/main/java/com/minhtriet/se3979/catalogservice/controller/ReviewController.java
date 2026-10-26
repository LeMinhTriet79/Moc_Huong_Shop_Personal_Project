package com.minhtriet.se3979.catalogservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minhtriet.se3979.catalogservice.dto.request.ReviewCreateRequest;
import com.minhtriet.se3979.catalogservice.dto.response.ApiResponse;
import com.minhtriet.se3979.catalogservice.dto.response.ReviewResponse;
import com.minhtriet.se3979.catalogservice.security.SecurityUtils;
import com.minhtriet.se3979.catalogservice.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/catalog/products")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final ObjectMapper objectMapper;

    /**
     * API 1: ĐĂNG ĐÁNH GIÁ MỚI (Dành cho Khách hàng đã đăng nhập)
     */
    @PostMapping(value = "/{productId}/reviews", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Object>> createReview(
            @PathVariable Long productId,
            @RequestPart("review") String reviewJson,
            @RequestPart(value = "images", required = false) List<MultipartFile> files) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            ReviewCreateRequest request = objectMapper.readValue(reviewJson, ReviewCreateRequest.class);
            Object response = reviewService.createReview(productId, userId, request, files);
            return ResponseEntity.ok(ApiResponse.success(response, "Đăng đánh giá thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "BAD_REQUEST", "Lỗi gửi đánh giá: " + e.getMessage()));
        }
    }

    /**
     * API 2: LẤY DANH SÁCH ĐÁNH GIÁ (Public cho khách xem chi tiết sản phẩm)
     */
    @GetMapping("/{productId}/reviews")
    public ResponseEntity<ApiResponse<Page<ReviewResponse>>> getProductReviews(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<ReviewResponse> result = reviewService.getProductReviews(productId, PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.success(result, "Lấy danh sách đánh giá thành công"));
    }

    // ==========================================
    // API CẬP NHẬT & XÓA BỞI KHÁCH HÀNG
    // ==========================================

    @PutMapping("/{productId}/reviews/{reviewId}")
    public ResponseEntity<ApiResponse<Object>> updateReview(
            @PathVariable Long productId,
            @PathVariable Long reviewId,
            @RequestParam Integer rating,
            @RequestParam String content) {
        Long userId = SecurityUtils.getCurrentUserId();
        Object updated = reviewService.updateReview(reviewId, userId, rating, content);
        return ResponseEntity.ok(ApiResponse.success(updated, "Cập nhật đánh giá thành công"));
    }

    @DeleteMapping("/{productId}/reviews/{reviewId}")
    public ResponseEntity<ApiResponse<Void>> deleteReview(
            @PathVariable Long productId,
            @PathVariable Long reviewId) {
        Long userId = SecurityUtils.getCurrentUserId();
        reviewService.deleteReview(reviewId, userId);
        return ResponseEntity.ok(ApiResponse.success(null, "Đã xóa đánh giá thành công!"));
    }

    // ==========================================
    // API QUẢN LÝ BỞI STAFF VÀ ADMIN
    // ==========================================

    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    @PutMapping("/reviews/{reviewId}/reply")
    public ResponseEntity<ApiResponse<Object>> adminReply(
            @PathVariable Long reviewId,
            @RequestParam String replyContent) {
        Object reply = reviewService.adminReplyReview(reviewId, replyContent);
        return ResponseEntity.ok(ApiResponse.success(reply, "Phản hồi đánh giá thành công"));
    }

    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    @PutMapping("/reviews/{reviewId}/toggle-visibility")
    public ResponseEntity<ApiResponse<Void>> toggleVisibility(@PathVariable Long reviewId) {
        reviewService.toggleReviewVisibility(reviewId);
        return ResponseEntity.ok(ApiResponse.success(null, "Đã thay đổi trạng thái hiển thị của đánh giá!"));
    }

    /**
     * API CHO STAFF & ADMIN: Lấy tất cả đánh giá (Bao gồm cả bị ẩn)
     */
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    @GetMapping("/{productId}/admin/reviews")
    public ResponseEntity<ApiResponse<Page<ReviewResponse>>> getAdminProductReviews(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<ReviewResponse> result = reviewService.getAdminProductReviews(productId, PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.success(result, "Lấy danh sách đánh giá quản trị thành công"));
    }
}