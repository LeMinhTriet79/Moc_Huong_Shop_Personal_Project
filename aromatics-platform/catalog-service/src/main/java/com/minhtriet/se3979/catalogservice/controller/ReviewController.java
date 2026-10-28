package com.minhtriet.se3979.catalogservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minhtriet.se3979.catalogservice.dto.request.ReviewCreateRequest;
import com.minhtriet.se3979.catalogservice.dto.response.ApiResponse;
import com.minhtriet.se3979.catalogservice.dto.response.ReviewResponse;
import com.minhtriet.se3979.catalogservice.exception.AppException;
import com.minhtriet.se3979.catalogservice.security.SecurityUtils;
import com.minhtriet.se3979.catalogservice.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/catalog")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final ObjectMapper objectMapper;

    /**
     * API 1A: ĐĂNG ĐÁNH GIÁ MỚI QUA JSON (Frontend: POST /api/catalog/reviews)
     */
    @PostMapping(value = "/reviews", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<ReviewResponse>> createReviewJson(
            @Valid @RequestBody ReviewCreateRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        ReviewResponse response = reviewService.createReview(request.getProductId(), userId, request, null);
        return ResponseEntity.ok(ApiResponse.success(response, "Đăng đánh giá thành công"));
    }

    /**
     * API 1B: ĐĂNG ĐÁNH GIÁ MỚI KÈM ẢNH MULTIPART
     */
    @PostMapping(value = {"/products/{productId}/reviews", "/reviews/{productId}/reviews"}, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ReviewResponse>> createReviewMultipart(
            @PathVariable Long productId,
            @RequestPart("review") String reviewJson,
            @RequestPart(value = "images", required = false) List<MultipartFile> files) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            ReviewCreateRequest request = objectMapper.readValue(reviewJson, ReviewCreateRequest.class);
            ReviewResponse response = reviewService.createReview(productId, userId, request, files);
            return ResponseEntity.ok(ApiResponse.success(response, "Đăng đánh giá thành công"));
        } catch (AppException ae) {
            throw ae;
        } catch (Exception e) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Lỗi gửi đánh giá: " + e.getMessage());
        }
    }

    /**
     * API 2: LẤY DANH SÁCH ĐÁNH GIÁ (Public cho khách xem chi tiết sản phẩm)
     */
    @GetMapping({"/products/{productId}/reviews", "/reviews/product/{productId}"})
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

    @PutMapping({"/products/{productId}/reviews/{reviewId}", "/reviews/{reviewId}"})
    public ResponseEntity<ApiResponse<Object>> updateReview(
            @PathVariable(required = false) Long productId,
            @PathVariable Long reviewId,
            @RequestParam(required = false) Integer rating,
            @RequestParam(required = false) String content,
            @RequestBody(required = false) Map<String, Object> body) {
        Long userId = SecurityUtils.getCurrentUserId();
        Integer r = rating;
        String c = content;
        if (body != null) {
            if (r == null && body.get("rating") != null) {
                r = Integer.valueOf(body.get("rating").toString());
            }
            if (c == null) {
                c = (String) body.get("content");
                if (c == null) c = (String) body.get("comment");
            }
        }
        Object updated = reviewService.updateReview(reviewId, userId, r, c);
        return ResponseEntity.ok(ApiResponse.success(updated, "Cập nhật đánh giá thành công"));
    }

    @DeleteMapping({"/products/{productId}/reviews/{reviewId}", "/reviews/{reviewId}"})
    public ResponseEntity<ApiResponse<Void>> deleteReview(
            @PathVariable(required = false) Long productId,
            @PathVariable Long reviewId) {
        Long userId = SecurityUtils.getCurrentUserId();
        reviewService.deleteReview(reviewId, userId);
        return ResponseEntity.ok(ApiResponse.success(null, "Đã xóa đánh giá thành công!"));
    }

    // ==========================================
    // API QUẢN LÝ BỞI STAFF VÀ ADMIN
    // ==========================================

    /**
     * API CHO STAFF & ADMIN: Lấy tất cả đánh giá với bộ lọc
     */
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    @GetMapping({"/reviews/admin/reviews", "/products/admin/reviews"})
    public ResponseEntity<ApiResponse<Page<ReviewResponse>>> getAdminReviews(
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) Integer rating,
            @RequestParam(required = false) Boolean isVisible,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<ReviewResponse> result = reviewService.getAdminReviews(productId, rating, isVisible, PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.success(result, "Lấy danh sách đánh giá quản trị thành công"));
    }

    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    @GetMapping("/products/{productId}/admin/reviews")
    public ResponseEntity<ApiResponse<Page<ReviewResponse>>> getAdminProductReviews(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<ReviewResponse> result = reviewService.getAdminProductReviews(productId, PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.success(result, "Lấy danh sách đánh giá quản trị thành công"));
    }

    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    @PutMapping({"/reviews/{reviewId}/reply", "/products/reviews/{reviewId}/reply"})
    public ResponseEntity<ApiResponse<ReviewResponse>> adminReply(
            @PathVariable Long reviewId,
            @RequestParam(required = false) String replyContent,
            @RequestBody(required = false) Map<String, String> body) {
        String reply = replyContent;
        if ((reply == null || reply.isBlank()) && body != null) {
            reply = body.get("replyMessage");
            if (reply == null) {
                reply = body.get("replyContent");
            }
        }
        if (reply == null || reply.isBlank()) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Nội dung phản hồi không được để trống");
        }
        ReviewResponse result = reviewService.adminReplyReview(reviewId, reply);
        return ResponseEntity.ok(ApiResponse.success(result, "Phản hồi đánh giá thành công"));
    }

    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    @PutMapping({"/reviews/{reviewId}/toggle-visibility", "/products/reviews/{reviewId}/toggle-visibility"})
    public ResponseEntity<ApiResponse<ReviewResponse>> toggleVisibility(@PathVariable Long reviewId) {
        ReviewResponse result = reviewService.toggleReviewVisibility(reviewId);
        return ResponseEntity.ok(ApiResponse.success(result, "Đã thay đổi trạng thái hiển thị của đánh giá!"));
    }
}