package com.minhtriet.se3979.orderservice.repository;

import com.minhtriet.se3979.orderservice.entity.CouponUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CouponUsageRepository extends JpaRepository<CouponUsage, Long> {

    long countByUserIdAndCouponIdAndIsRefundedFalse(Long userId, Long couponId);

    List<CouponUsage> findByOrderId(Long orderId);

    Optional<CouponUsage> findByOrderIdAndIsRefundedFalse(Long orderId);

    List<CouponUsage> findByUserIdOrderByUsedAtDesc(Long userId);
}
