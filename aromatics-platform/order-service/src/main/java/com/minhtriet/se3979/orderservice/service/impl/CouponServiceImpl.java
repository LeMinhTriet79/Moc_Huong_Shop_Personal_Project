package com.minhtriet.se3979.orderservice.service.impl;

import com.minhtriet.se3979.orderservice.dto.request.CouponRequest;
import com.minhtriet.se3979.orderservice.dto.response.CouponCalculationResult;
import com.minhtriet.se3979.orderservice.dto.response.CouponResponse;
import com.minhtriet.se3979.orderservice.entity.Coupon;
import com.minhtriet.se3979.orderservice.entity.CouponUsage;
import com.minhtriet.se3979.orderservice.entity.Order;
import com.minhtriet.se3979.orderservice.exception.AppException;
import com.minhtriet.se3979.orderservice.redis.CartItem;
import com.minhtriet.se3979.orderservice.repository.CouponRepository;
import com.minhtriet.se3979.orderservice.repository.CouponUsageRepository;
import com.minhtriet.se3979.orderservice.service.CouponService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CouponServiceImpl implements CouponService {

    private final CouponRepository couponRepository;
    private final CouponUsageRepository couponUsageRepository;

    @Override
    public List<CouponResponse> getAvailableCoupons() {
        return couponRepository.findAvailableCoupons(LocalDateTime.now()).stream()
                .map(CouponResponse::fromEntity)
                .toList();
    }

    @Override
    public CouponCalculationResult validateAndCalculateDiscount(
            Long userId, String couponCode, BigDecimal subtotal, BigDecimal shippingFee, List<CartItem> items) {

        if (couponCode == null || couponCode.isBlank()) {
            return CouponCalculationResult.builder()
                    .couponCode(couponCode)
                    .discountAmount(BigDecimal.ZERO)
                    .isValid(false)
                    .message("Mã giảm giá không được để trống")
                    .build();
        }

        Optional<Coupon> opt = couponRepository.findByCode(couponCode.trim().toUpperCase());
        if (opt.isEmpty()) {
            return CouponCalculationResult.builder()
                    .couponCode(couponCode)
                    .discountAmount(BigDecimal.ZERO)
                    .isValid(false)
                    .message("Mã giảm giá không tồn tại")
                    .build();
        }

        Coupon coupon = opt.get();
        if (!Boolean.TRUE.equals(coupon.getIsActive())) {
            return CouponCalculationResult.builder()
                    .couponCode(couponCode)
                    .discountAmount(BigDecimal.ZERO)
                    .isValid(false)
                    .message("Mã giảm giá hiện không hoạt động")
                    .build();
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(coupon.getStartsAt())) {
            return CouponCalculationResult.builder()
                    .couponCode(couponCode)
                    .discountAmount(BigDecimal.ZERO)
                    .isValid(false)
                    .message("Mã giảm giá chưa đến thời gian áp dụng")
                    .build();
        }

        if (coupon.getExpiresAt() != null && now.isAfter(coupon.getExpiresAt())) {
            return CouponCalculationResult.builder()
                    .couponCode(couponCode)
                    .discountAmount(BigDecimal.ZERO)
                    .isValid(false)
                    .message("Mã giảm giá đã hết hạn sử dụng")
                    .build();
        }

        if (coupon.getMaxUses() != null && coupon.getUsedCount() >= coupon.getMaxUses()) {
            return CouponCalculationResult.builder()
                    .couponCode(couponCode)
                    .discountAmount(BigDecimal.ZERO)
                    .isValid(false)
                    .message("Mã giảm giá đã hết lượt sử dụng trên hệ thống")
                    .build();
        }

        if (userId != null) {
            long usedByUser = couponUsageRepository.countByUserIdAndCouponIdAndIsRefundedFalse(userId, coupon.getId());
            if (usedByUser >= coupon.getMaxUsesPerUser()) {
                return CouponCalculationResult.builder()
                        .couponCode(couponCode)
                        .discountAmount(BigDecimal.ZERO)
                        .isValid(false)
                        .message("Bạn đã dùng hết số lượt cho phép (" + coupon.getMaxUsesPerUser() + " lần) của mã này")
                        .build();
            }
        }

        if (subtotal != null && coupon.getMinOrderValue() != null && subtotal.compareTo(coupon.getMinOrderValue()) < 0) {
            return CouponCalculationResult.builder()
                    .couponCode(couponCode)
                    .discountAmount(BigDecimal.ZERO)
                    .isValid(false)
                    .message("Đơn hàng chưa đạt giá trị tối thiểu " + coupon.getMinOrderValue() + " đ để áp dụng mã")
                    .build();
        }

        if (coupon.getApplicableProductIds() != null && !coupon.getApplicableProductIds().isEmpty()) {
            boolean hasApplicableProduct = items != null && items.stream()
                    .anyMatch(item -> coupon.getApplicableProductIds().contains(item.getProductId()));
            if (!hasApplicableProduct) {
                return CouponCalculationResult.builder()
                        .couponCode(couponCode)
                        .discountAmount(BigDecimal.ZERO)
                        .isValid(false)
                        .message("Mã giảm giá không áp dụng cho sản phẩm nào trong giỏ hàng của bạn")
                        .build();
            }
        }

        // Tính toán số tiền được giảm
        BigDecimal discount = BigDecimal.ZERO;
        BigDecimal validSubtotal = subtotal != null ? subtotal : BigDecimal.ZERO;
        BigDecimal validShipping = shippingFee != null ? shippingFee : BigDecimal.ZERO;

        switch (coupon.getType()) {
            case PERCENTAGE -> {
                discount = validSubtotal.multiply(coupon.getValue()).divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);
                if (coupon.getMaxDiscountAmount() != null && discount.compareTo(coupon.getMaxDiscountAmount()) > 0) {
                    discount = coupon.getMaxDiscountAmount();
                }
            }
            case FIXED_AMOUNT -> discount = coupon.getValue().min(validSubtotal);
            case FREE_SHIPPING -> discount = coupon.getValue().min(validShipping);
        }

        return CouponCalculationResult.builder()
                .couponCode(coupon.getCode())
                .discountAmount(discount)
                .isValid(true)
                .message("Áp dụng mã giảm giá thành công")
                .build();
    }

    @Transactional
    @Override
    public CouponUsage recordCouponUsage(String couponCode, Long userId, Order order, BigDecimal discountAmount) {
        if (couponCode == null || couponCode.isBlank()) return null;

        Coupon coupon = couponRepository.findByCode(couponCode.trim().toUpperCase())
                .orElse(null);
        if (coupon == null) return null;

        coupon.setUsedCount(coupon.getUsedCount() + 1);
        couponRepository.save(coupon);

        CouponUsage usage = CouponUsage.builder()
                .coupon(coupon)
                .userId(userId)
                .order(order)
                .discountAmount(discountAmount != null ? discountAmount : BigDecimal.ZERO)
                .isRefunded(false)
                .build();

        return couponUsageRepository.save(usage);
    }

    @Transactional
    @Override
    public void refundCouponUsage(Long orderId) {
        couponUsageRepository.findByOrderIdAndIsRefundedFalse(orderId).ifPresent(usage -> {
            usage.setIsRefunded(true);
            couponUsageRepository.save(usage);

            Coupon coupon = usage.getCoupon();
            if (coupon != null && coupon.getUsedCount() > 0) {
                coupon.setUsedCount(coupon.getUsedCount() - 1);
                couponRepository.save(coupon);
            }
            log.info("Đã hoàn lại lượt sử dụng mã giảm giá '{}' cho đơn hàng id: {}", coupon != null ? coupon.getCode() : "N/A", orderId);
        });
    }

    @Transactional
    @Override
    public CouponResponse createCoupon(CouponRequest request) {
        String code = request.getCode().trim().toUpperCase();
        if (couponRepository.existsByCode(code)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Mã giảm giá '" + code + "' đã tồn tại!");
        }

        Coupon coupon = Coupon.builder()
                .code(code)
                .name(request.getName())
                .type(request.getType())
                .value(request.getValue())
                .maxDiscountAmount(request.getMaxDiscountAmount())
                .minOrderValue(request.getMinOrderValue() != null ? request.getMinOrderValue() : BigDecimal.ZERO)
                .maxUses(request.getMaxUses())
                .maxUsesPerUser(request.getMaxUsesPerUser() != null ? request.getMaxUsesPerUser() : 1)
                .usedCount(0)
                .applicableProductIds(request.getApplicableProductIds())
                .applicableCategoryIds(request.getApplicableCategoryIds())
                .startsAt(request.getStartsAt())
                .expiresAt(request.getExpiresAt())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        Coupon saved = couponRepository.save(coupon);
        return CouponResponse.fromEntity(saved);
    }

    @Transactional
    @Override
    public CouponResponse updateCoupon(Long id, CouponRequest request) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy mã giảm giá với id: " + id));

        String code = request.getCode().trim().toUpperCase();
        if (!code.equalsIgnoreCase(coupon.getCode()) && couponRepository.existsByCode(code)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Mã giảm giá '" + code + "' đã được sử dụng!");
        }

        coupon.setCode(code);
        coupon.setName(request.getName());
        coupon.setType(request.getType());
        coupon.setValue(request.getValue());
        coupon.setMaxDiscountAmount(request.getMaxDiscountAmount());
        coupon.setMinOrderValue(request.getMinOrderValue() != null ? request.getMinOrderValue() : BigDecimal.ZERO);
        coupon.setMaxUses(request.getMaxUses());
        coupon.setMaxUsesPerUser(request.getMaxUsesPerUser() != null ? request.getMaxUsesPerUser() : 1);
        coupon.setApplicableProductIds(request.getApplicableProductIds());
        coupon.setApplicableCategoryIds(request.getApplicableCategoryIds());
        coupon.setStartsAt(request.getStartsAt());
        coupon.setExpiresAt(request.getExpiresAt());
        if (request.getIsActive() != null) {
            coupon.setIsActive(request.getIsActive());
        }

        Coupon saved = couponRepository.save(coupon);
        return CouponResponse.fromEntity(saved);
    }

    @Transactional
    @Override
    public void deleteCoupon(Long id) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy mã giảm giá với id: " + id));

        coupon.setIsActive(false);
        couponRepository.save(coupon);
    }

    @Override
    public Page<CouponResponse> getAllCoupons(Pageable pageable) {
        return couponRepository.findAll(pageable).map(CouponResponse::fromEntity);
    }

    @Override
    public CouponResponse getCouponById(Long id) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy mã giảm giá với id: " + id));
        return CouponResponse.fromEntity(coupon);
    }
}
