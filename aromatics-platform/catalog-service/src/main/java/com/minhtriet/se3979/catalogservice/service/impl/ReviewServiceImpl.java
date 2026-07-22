package com.minhtriet.se3979.catalogservice.service.impl;

import com.minhtriet.se3979.catalogservice.dto.request.ReviewCreateRequest;
import com.minhtriet.se3979.catalogservice.dto.response.ReviewResponse;
import com.minhtriet.se3979.catalogservice.entity.*;
import com.minhtriet.se3979.catalogservice.repository.*;
import com.minhtriet.se3979.catalogservice.service.CloudinaryService;
import com.minhtriet.se3979.catalogservice.service.ReviewService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    private final CloudinaryService cloudinaryService;

    @Transactional
    @Override
    public Object createReview(Long productId, Long userId, ReviewCreateRequest request, List<MultipartFile> files) {

        // 1. FAIL FAST: Kiểm tra chống Spam (Mỗi order_item chỉ được đánh giá 1 lần)
        if (reviewRepository.existsByOrderItemId(request.getOrderItemId())) {
            throw new RuntimeException("Bạn đã đánh giá cho sản phẩm này trong đơn hàng rồi!");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm!"));

        ProductVariant variant = variantRepository.findById(request.getVariantId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiên bản sản phẩm!"));

        // Giỏ đựng rác tạm thời (Lưu ID ảnh Cloudinary)
        List<String> uploadedCloudinaryIds = new ArrayList<>();

        try {
            // 2. TẠO THỰC THỂ REVIEW
            Review review = Review.builder()
                    .product(product)
                    .variant(variant)
                    .userId(userId)
                    .orderItemId(request.getOrderItemId())
                    .rating(request.getRating())
                    .title(request.getTitle())
                    .content(request.getContent())
                    .isVisible(true)
                    .build();

            // 3. XỬ LÝ UP ẢNH (Nếu khách hàng có đính kèm ảnh)
            if (files != null && !files.isEmpty()) {
                List<ReviewImage> reviewImages = new ArrayList<>();

                for (MultipartFile file : files) {
                    if (file.isEmpty()) continue;

                    Map<String, Object> uploadResult = cloudinaryService.uploadImage(file);
                    String publicId = uploadResult.get("public_id").toString();

                    uploadedCloudinaryIds.add(publicId); // Bỏ vào giỏ rác tạm

                    ReviewImage image = ReviewImage.builder()
                            .review(review) // Nối Khóa ngoại
                            .cloudinaryPublicId(publicId)
                            .imageUrl(uploadResult.get("secure_url").toString())
                            .build();
                    reviewImages.add(image);
                }
                review.setImages(reviewImages);
            }

            // 4. LƯU REVIEW VÀ ÉP FLUSH ĐỂ DB NHẬN DIỆN NGAY LẬP TỨC
            reviewRepository.saveAndFlush(review);

            // 5. CẬP NHẬT ĐIỂM SỐ (Gọi Hàm Aggregate xịn sò trong Repository)
            ReviewRepository.ReviewStats stats = reviewRepository.getReviewStats(productId);

            product.setTotalReviews(stats.getTotalReviews());
            // Làm tròn 2 chữ số thập phân cho đẹp (VD: 4.50, 4.67)
            product.setAverageRating(BigDecimal.valueOf(stats.getAverageRating()));

            productRepository.save(product);

            return "Đánh giá sản phẩm thành công! Cảm ơn bạn đã phản hồi.";

        } catch (Exception e) {
            // 6. COMPENSATING TRANSACTION: Lỗi DB thì leo lên mây dọn rác
            if (!uploadedCloudinaryIds.isEmpty()) {
                for (String publicId : uploadedCloudinaryIds) {
                    try {
                        cloudinaryService.deleteImage(publicId);
                    } catch (Exception ex) {
                        System.err.println("Lỗi dọn rác Cloudinary (Review): " + ex.getMessage());
                    }
                }
            }
            throw new RuntimeException("Lỗi trong quá trình gửi đánh giá: " + e.getMessage());
        }
    }

    // ==========================================
    // API LẤY DANH SÁCH CHO KHÁCH HÀNG (CHỈ THẤY BÀI HIỂN THỊ)
    // ==========================================
    @Override
    public Page<ReviewResponse> getProductReviews(Long productId, Pageable pageable) {
        Page<Review> reviews = reviewRepository.findByProductIdAndIsVisibleTrue(productId, pageable);
        return reviews.map(this::mapToReviewResponse);
    }

    // ==========================================
    // API LẤY DANH SÁCH CHO ADMIN (THẤY TẤT CẢ)
    // ==========================================
    @Override
    public Page<ReviewResponse> getAdminProductReviews(Long productId, Pageable pageable) {
        Page<Review> reviews = reviewRepository.findByProductId(productId, pageable);
        return reviews.map(this::mapToReviewResponse);
    }

    // Hàm Helper: Map từ Entity sang DTO (Tránh lặp code)
    private ReviewResponse mapToReviewResponse(Review review) {
        List<String> imageUrls = review.getImages() != null ?
                review.getImages().stream().map(ReviewImage::getImageUrl).toList() :
                new ArrayList<>();

        return ReviewResponse.builder()
                .id(review.getId())
                .userId(review.getUserId())
                .variantName(review.getVariant().getVariantName())
                .rating(review.getRating())
                .title(review.getTitle())
                .content(review.getContent())
                .images(imageUrls)
                .adminReply(review.getAdminReply())
                .isVisible(review.getIsVisible()) // Đã bổ sung cờ trạng thái
                .createdAt(review.getCreatedAt())
                .build();
    }
    // ==========================================
    // CÁC HÀM CRUD BỔ SUNG CHO ĐÁNH GIÁ
    // ==========================================

    @Transactional
    @Override
    public Object updateReview(Long reviewId, Long userId, Integer rating, String content) {
        Review review = reviewRepository.findByIdAndUserId(reviewId, userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đánh giá hoặc bạn không có quyền sửa!"));

        review.setRating(rating);
        review.setContent(content);
        reviewRepository.saveAndFlush(review);

        // Tính lại điểm số
        recalculateProductRating(review.getProduct());
        return "Cập nhật đánh giá thành công!";
    }

    @Transactional
    @Override
    public void deleteReview(Long reviewId, Long userId) {
        Review review = reviewRepository.findByIdAndUserId(reviewId, userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đánh giá hoặc bạn không có quyền xóa!"));

        Product product = review.getProduct();

        // 1. Lên mây Cloudinary dọn rác ảnh
        if (review.getImages() != null && !review.getImages().isEmpty()) {
            for (ReviewImage img : review.getImages()) {
                try {
                    cloudinaryService.deleteImage(img.getCloudinaryPublicId());
                } catch (Exception e) {
                    System.err.println("Lỗi dọn rác ảnh review: " + e.getMessage());
                }
            }
        }

        // 2. Xóa dưới DB
        reviewRepository.delete(review);
        reviewRepository.flush(); // Ép xóa ngay lập tức để tính lại điểm

        // 3. Tính lại điểm số của Sản phẩm
        recalculateProductRating(product);
    }

    @Transactional
    @Override
    public Object adminReplyReview(Long reviewId, String replyContent) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đánh giá!"));

        review.setAdminReply(replyContent);
        review.setAdminRepliedAt(java.time.LocalDateTime.now());
        reviewRepository.save(review);
        return "Đã trả lời bình luận của khách hàng!";
    }

    @Transactional
    @Override
    public void toggleReviewVisibility(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đánh giá!"));

        // Đảo ngược trạng thái Ẩn/Hiện
        review.setIsVisible(!review.getIsVisible());
        reviewRepository.saveAndFlush(review);

        // Ẩn đi thì điểm số Sản phẩm cũng phải tính lại, không được tính bình luận đã bị ẩn
        recalculateProductRating(review.getProduct());
    }

    // --- Hàm Helper dùng chung để tính toán lại điểm số ---
    private void recalculateProductRating(Product product) {
        ReviewRepository.ReviewStats stats = reviewRepository.getReviewStats(product.getId());
        product.setTotalReviews(stats.getTotalReviews());
        product.setAverageRating(BigDecimal.valueOf(stats.getAverageRating()));
        productRepository.save(product);
    }
}