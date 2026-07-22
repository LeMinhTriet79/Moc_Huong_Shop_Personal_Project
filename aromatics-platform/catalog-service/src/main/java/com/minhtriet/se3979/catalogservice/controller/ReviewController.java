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
}