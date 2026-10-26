package com.minhtriet.se3979.catalogservice.service.impl;

import com.minhtriet.se3979.catalogservice.dto.request.ReviewCreateRequest;
import com.minhtriet.se3979.catalogservice.dto.response.ReviewResponse;
import com.minhtriet.se3979.catalogservice.entity.*;
import com.minhtriet.se3979.catalogservice.exception.AppException;
import com.minhtriet.se3979.catalogservice.repository.*;
import com.minhtriet.se3979.catalogservice.service.CloudinaryService;
import com.minhtriet.se3979.catalogservice.service.ProductCacheService;
import com.minhtriet.se3979.catalogservice.service.ReviewService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    private final CloudinaryService cloudinaryService;
    private final ProductCacheService productCacheService;

    @Transactional
    @Override
    public Object createReview(Long productId, Long userId, ReviewCreateRequest request, List<MultipartFile> files) {

        // 1. FAIL FAST: Mỗi order_item chỉ được đánh giá 1 lần
        if (reviewRepository.existsByOrderItemId(request.getOrderItemId())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Bạn đã đánh giá cho sản phẩm này trong đơn hàng rồi!");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy sản phẩm!"));

        ProductVariant variant = variantRepository.findById(request.getVariantId())
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy phiên bản sản phẩm!"));

        List<String> uploadedCloudinaryIds = new ArrayList<>();

        try {
            // 2. Tạo thực thể Review
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

            // 3. Xử lý up ảnh
            if (files != null && !files.isEmpty()) {
                List<ReviewImage> reviewImages = new ArrayList<>();

                for (MultipartFile file : files) {
                    if (file.isEmpty()) continue;

                    Map<String, Object> uploadResult = cloudinaryService.uploadImage(file);
                    String publicId = uploadResult.get("public_id").toString();
                    uploadedCloudinaryIds.add(publicId);

                    ReviewImage image = ReviewImage.builder()
                            .review(review)
                            .cloudinaryPublicId(publicId)
                            .imageUrl(uploadResult.get("secure_url").toString())
                            .build();
                    reviewImages.add(image);
                }
                review.setImages(reviewImages);
            }

            reviewRepository.saveAndFlush(review);

            // 4. Cập nhật điểm số và xóa cache
            recalculateProductRating(product);

            return "Đánh giá sản phẩm thành công! Cảm ơn bạn đã phản hồi.";

        } catch (Exception e) {
            // Compensating Transaction
            if (!uploadedCloudinaryIds.isEmpty()) {
                for (String publicId : uploadedCloudinaryIds) {
                    try {
                        cloudinaryService.deleteImage(publicId);
                    } catch (Exception ex) {
                        log.error("Lỗi dọn rác Cloudinary (Review): {}", ex.getMessage());
                    }
                }
            }
            if (e instanceof AppException) throw (AppException) e;
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Lỗi trong quá trình gửi đánh giá: " + e.getMessage());
        }
    }

    @Override
    public Page<ReviewResponse> getProductReviews(Long productId, Pageable pageable) {
        Page<Review> reviews = reviewRepository.findByProductIdAndIsVisibleTrue(productId, pageable);
        return reviews.map(this::mapToReviewResponse);
    }

    @Override
    public Page<ReviewResponse> getAdminProductReviews(Long productId, Pageable pageable) {
        Page<Review> reviews = reviewRepository.findByProductId(productId, pageable);
        return reviews.map(this::mapToReviewResponse);
    }

    private ReviewResponse mapToReviewResponse(Review review) {
        List<String> imageUrls = review.getImages() != null ?
                review.getImages().stream().map(ReviewImage::getImageUrl).toList() :
                new ArrayList<>();

        String variantName = review.getVariant() != null ? review.getVariant().getVariantName() : null;

        return ReviewResponse.builder()
                .id(review.getId())
                .userId(review.getUserId())
                .variantName(variantName)
                .rating(review.getRating())
                .title(review.getTitle())
                .content(review.getContent())
                .images(imageUrls)
                .adminReply(review.getAdminReply())
                .isVisible(review.getIsVisible())
                .createdAt(review.getCreatedAt())
                .build();
    }

    @Transactional
    @Override
    public Object updateReview(Long reviewId, Long userId, Integer rating, String content) {
        Review review = reviewRepository.findByIdAndUserId(reviewId, userId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy đánh giá hoặc bạn không có quyền sửa!"));

        review.setRating(rating);
        review.setContent(content);
        reviewRepository.saveAndFlush(review);

        recalculateProductRating(review.getProduct());
        return "Cập nhật đánh giá thành công!";
    }

    @Transactional
    @Override
    public void deleteReview(Long reviewId, Long userId) {
        Review review = reviewRepository.findByIdAndUserId(reviewId, userId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy đánh giá hoặc bạn không có quyền xóa!"));

        Product product = review.getProduct();

        if (review.getImages() != null && !review.getImages().isEmpty()) {
            for (ReviewImage img : review.getImages()) {
                try {
                    cloudinaryService.deleteImage(img.getCloudinaryPublicId());
                } catch (Exception e) {
                    log.error("Lỗi dọn rác ảnh review: {}", e.getMessage());
                }
            }
        }

        reviewRepository.delete(review);
        reviewRepository.flush();

        recalculateProductRating(product);
    }

    @Transactional
    @Override
    public Object adminReplyReview(Long reviewId, String replyContent) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy đánh giá!"));

        review.setAdminReply(replyContent);
        review.setAdminRepliedAt(LocalDateTime.now());
        reviewRepository.save(review);
        return "Đã trả lời bình luận của khách hàng!";
    }

    @Transactional
    @Override
    public void toggleReviewVisibility(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy đánh giá!"));

        review.setIsVisible(!review.getIsVisible());
        reviewRepository.saveAndFlush(review);

        recalculateProductRating(review.getProduct());
    }

    private void recalculateProductRating(Product product) {
        ReviewRepository.ReviewStats stats = reviewRepository.getReviewStats(product.getId());
        product.setTotalReviews(stats.getTotalReviews());
        product.setAverageRating(BigDecimal.valueOf(stats.getAverageRating()));
        productRepository.save(product);

        // Xóa cache khi cập nhật rating
        productCacheService.evictProduct(product.getSlug());
    }
}