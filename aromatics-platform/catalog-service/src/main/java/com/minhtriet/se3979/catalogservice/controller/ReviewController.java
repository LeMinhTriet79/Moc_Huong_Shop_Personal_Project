package com.minhtriet.se3979.catalogservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minhtriet.se3979.catalogservice.dto.request.ReviewCreateRequest;
import com.minhtriet.se3979.catalogservice.dto.response.ReviewResponse;
import com.minhtriet.se3979.catalogservice.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
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
     * API 1: ĐĂNG ĐÁNH GIÁ MỚI (Dành cho Khách hàng)
     * Sinh ra lỗi 400 nếu khách hàng spam hoặc chưa mua hàng.
     */
    @PostMapping(value = "/{productId}/reviews", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createReview(
            @PathVariable Long productId,
            // Giả lập nhận ID User từ API Gateway truyền xuống (Mặc định là 1 để bạn dễ test Swagger)
            @RequestHeader(value = "X-UserId", defaultValue = "1") Long userId,
            @RequestPart("review") String reviewJson,
            @RequestPart(value = "images", required = false) List<MultipartFile> files) {
        try {
            // Dịch chuỗi String thành Object DTO
            ReviewCreateRequest request = objectMapper.readValue(reviewJson, ReviewCreateRequest.class);

            // Đẩy xuống Service xử lý
            Object response = reviewService.createReview(productId, userId, request, files);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi gửi đánh giá: " + e.getMessage());
        }
    }

    /**
     * API 2: LẤY DANH SÁCH ĐÁNH GIÁ (Phục vụ hiển thị dưới đáy trang Chi tiết Sản phẩm)
     * Ai cũng xem được, hỗ trợ phân trang mượt mà.
     */
    @GetMapping("/{productId}/reviews")
    public ResponseEntity<Page<ReviewResponse>> getProductReviews(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(reviewService.getProductReviews(productId, PageRequest.of(page, size)));
    }

    // ==========================================
    // API CẬP NHẬT & XÓA BỞI KHÁCH HÀNG
    // ==========================================

    @PutMapping("/{productId}/reviews/{reviewId}")
    public ResponseEntity<?> updateReview(
            @PathVariable Long productId,
            @PathVariable Long reviewId,
            @RequestHeader(value = "X-UserId", defaultValue = "1") Long userId,
            @RequestParam Integer rating,
            @RequestParam String content) {
        return ResponseEntity.ok(reviewService.updateReview(reviewId, userId, rating, content));
    }

    @DeleteMapping("/{productId}/reviews/{reviewId}")
    public ResponseEntity<?> deleteReview(
            @PathVariable Long productId,
            @PathVariable Long reviewId,
            @RequestHeader(value = "X-UserId", defaultValue = "1") Long userId) {
        reviewService.deleteReview(reviewId, userId);
        return ResponseEntity.ok("Đã xóa đánh giá thành công!");
    }

    // ==========================================
    // API QUẢN LÝ BỞI ADMIN
    // ==========================================

    @PutMapping("/reviews/{reviewId}/reply")
    public ResponseEntity<?> adminReply(
            @PathVariable Long reviewId,
            @RequestParam String replyContent) {
        // Trong thực tế sẽ gắn @PreAuthorize("hasRole('ADMIN')") ở đây
        return ResponseEntity.ok(reviewService.adminReplyReview(reviewId, replyContent));
    }

    @PutMapping("/reviews/{reviewId}/toggle-visibility")
    public ResponseEntity<?> toggleVisibility(@PathVariable Long reviewId) {
        // Trong thực tế sẽ gắn @PreAuthorize("hasRole('ADMIN')") ở đây
        reviewService.toggleReviewVisibility(reviewId);
        return ResponseEntity.ok("Đã thay đổi trạng thái hiển thị của đánh giá!");
    }

    /**
     * API CHO ADMIN: Lấy tất cả đánh giá (Bao gồm cả bị ẩn)
     */
    @GetMapping("/{productId}/admin/reviews")
    public ResponseEntity<Page<ReviewResponse>> getAdminProductReviews(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(reviewService.getAdminProductReviews(productId, PageRequest.of(page, size)));
    }
}