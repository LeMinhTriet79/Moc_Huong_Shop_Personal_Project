package com.minhtriet.se3979.catalogservice.service;

import com.minhtriet.se3979.catalogservice.dto.request.ReviewCreateRequest;
import com.minhtriet.se3979.catalogservice.dto.response.ReviewResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ReviewService {

    // Đăng đánh giá mới
    Object createReview(Long productId, Long userId, ReviewCreateRequest request, List<MultipartFile> files);

    // Lấy danh sách đánh giá của 1 sản phẩm
    Page<ReviewResponse> getProductReviews(Long productId, Pageable pageable);

    // 1. Khách hàng: Cập nhật nội dung đánh giá (giữ nguyên ảnh)
    Object updateReview(Long reviewId, Long userId, Integer rating, String content);

    // 2. Khách hàng: Xóa đánh giá (Dọn sạch DB và Cloudinary)
    void deleteReview(Long reviewId, Long userId);

    // 3. Admin: Trả lời đánh giá
    Object adminReplyReview(Long reviewId, String replyContent);

    // 4. Admin: Ẩn/Hiện đánh giá (Kiểm duyệt)
    void toggleReviewVisibility(Long reviewId);

    // Lấy danh sách đánh giá cho Admin (Thấy cả bài bị ẩn)
    Page<ReviewResponse> getAdminProductReviews(Long productId, Pageable pageable);
}