package com.minhtriet.se3979.catalogservice.repository;

import com.minhtriet.se3979.catalogservice.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    // 1. Lấy danh sách comment cho 1 sản phẩm (Chỉ lấy những comment chưa bị Admin ẩn)
    Page<Review> findByProductIdAndIsVisibleTrue(Long productId, Pageable pageable);

    // 2. Chặn Spam: Kiểm tra xem món hàng này trong đơn hàng đã được đánh giá chưa?
    boolean existsByOrderItemId(Long orderItemId);

    // =========================================================================
    // 3. KỸ THUẬT SPRING DATA PROJECTION: ÉP DATABASE TÍNH TOÁN
    // =========================================================================
    interface ReviewStats {
        Integer getTotalReviews();
        Double getAverageRating();
    }

    @Query("SELECT CAST(COUNT(r) AS int) as totalReviews, COALESCE(AVG(r.rating), 0.0) as averageRating " +
            "FROM Review r WHERE r.product.id = :productId AND r.isVisible = true")
    ReviewStats getReviewStats(@Param("productId") Long productId);

}