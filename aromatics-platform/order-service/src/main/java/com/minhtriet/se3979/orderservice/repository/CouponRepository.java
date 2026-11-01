package com.minhtriet.se3979.orderservice.repository;

import com.minhtriet.se3979.orderservice.entity.Coupon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long> {

    Optional<Coupon> findByCode(String code);

    Optional<Coupon> findByCodeIgnoreCase(String code);

    Optional<Coupon> findByCodeAndIsActiveTrue(String code);

    boolean existsByCode(String code);

    @Modifying
    @Query("UPDATE Coupon c SET c.usedCount = c.usedCount + 1 WHERE c.id = :id AND (c.maxUses IS NULL OR c.usedCount < c.maxUses)")
    int incrementUsedCount(@Param("id") Long id);

    @Modifying
    @Query("UPDATE Coupon c SET c.usedCount = c.usedCount - 1 WHERE c.id = :id AND c.usedCount > 0")
    int decrementUsedCount(@Param("id") Long id);

    @Query("SELECT c FROM Coupon c WHERE c.isActive = true " +
            "AND c.startsAt <= :now " +
            "AND (c.expiresAt IS NULL OR c.expiresAt >= :now) " +
            "AND (c.maxUses IS NULL OR c.usedCount < c.maxUses)")
    List<Coupon> findAvailableCoupons(@Param("now") LocalDateTime now);

    Page<Coupon> findAll(Pageable pageable);
}
