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
}