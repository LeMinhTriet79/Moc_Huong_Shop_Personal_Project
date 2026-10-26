package com.minhtriet.se3979.orderservice.service;

import com.minhtriet.se3979.orderservice.dto.request.CouponRequest;
import com.minhtriet.se3979.orderservice.dto.response.CouponCalculationResult;
import com.minhtriet.se3979.orderservice.dto.response.CouponResponse;
import com.minhtriet.se3979.orderservice.entity.Coupon;
import com.minhtriet.se3979.orderservice.exception.AppException;
import com.minhtriet.se3979.orderservice.repository.CouponRepository;
import com.minhtriet.se3979.orderservice.repository.CouponUsageRepository;
import com.minhtriet.se3979.orderservice.service.impl.CouponServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CouponServiceImplTest {

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private CouponUsageRepository couponUsageRepository;

    @InjectMocks
    private CouponServiceImpl couponService;

    private Coupon percentageCoupon;
    private Coupon freeShippingCoupon;
    private Coupon fixedAmountCoupon;

    @BeforeEach
    void setUp() {
        percentageCoupon = Coupon.builder()
                .id(1L)
                .code("MOCHUONG10")
                .name("Giảm 10%")
                .type(Coupon.CouponType.PERCENTAGE)
                .value(new BigDecimal("10.00"))
                .maxDiscountAmount(new BigDecimal("50000.00"))
                .minOrderValue(new BigDecimal("200000.00"))
                .maxUses(100)
                .maxUsesPerUser(2)
                .usedCount(10)
                .isActive(true)
                .startsAt(LocalDateTime.now().minusDays(1))
                .expiresAt(LocalDateTime.now().plusDays(10))
                .build();

        freeShippingCoupon = Coupon.builder()
                .id(2L)
                .code("FREESHIP")
                .name("Miễn phí vận chuyển")
                .type(Coupon.CouponType.FREE_SHIPPING)
                .value(new BigDecimal("30000.00"))
                .minOrderValue(new BigDecimal("300000.00"))
                .maxUses(50)
                .maxUsesPerUser(1)
                .usedCount(5)
                .isActive(true)
                .startsAt(LocalDateTime.now().minusDays(1))
                .expiresAt(LocalDateTime.now().plusDays(10))
                .build();

        fixedAmountCoupon = Coupon.builder()
                .id(3L)
                .code("TRAMHUONG50K")
                .name("Giảm 50k")
                .type(Coupon.CouponType.FIXED_AMOUNT)
                .value(new BigDecimal("50000.00"))
                .minOrderValue(new BigDecimal("500000.00"))
                .maxUses(100)
                .maxUsesPerUser(1)
                .usedCount(0)
                .isActive(true)
                .startsAt(LocalDateTime.now().minusDays(1))
                .expiresAt(LocalDateTime.now().plusDays(10))
                .build();
    }

    @Test
    @DisplayName("Tính giảm giá phần trăm thành công (10% của 300,000 = 30,000)")
    void testCalculateDiscount_Percentage_Success() {
        when(couponRepository.findByCode("MOCHUONG10")).thenReturn(Optional.of(percentageCoupon));
        when(couponUsageRepository.countByUserIdAndCouponIdAndIsRefundedFalse(1L, 1L)).thenReturn(0L);

        CouponCalculationResult result = couponService.validateAndCalculateDiscount(
                1L, "MOCHUONG10", new BigDecimal("300000.00"), new BigDecimal("30000.00"), Collections.emptyList()
        );

        assertTrue(result.isValid());
        assertEquals(new BigDecimal("30000"), result.getDiscountAmount());
    }

    @Test
    @DisplayName("Tính giảm giá phần trăm bị giới hạn bởi maxDiscountAmount (10% của 1,000,000 = 100,000 nhưng max 50,000)")
    void testCalculateDiscount_Percentage_CappedByMax() {
        when(couponRepository.findByCode("MOCHUONG10")).thenReturn(Optional.of(percentageCoupon));
        when(couponUsageRepository.countByUserIdAndCouponIdAndIsRefundedFalse(1L, 1L)).thenReturn(0L);

        CouponCalculationResult result = couponService.validateAndCalculateDiscount(
                1L, "MOCHUONG10", new BigDecimal("1000000.00"), new BigDecimal("30000.00"), Collections.emptyList()
        );

        assertTrue(result.isValid());
        assertEquals(new BigDecimal("50000.00"), result.getDiscountAmount());
    }

    @Test
    @DisplayName("Tính giảm giá Free Shipping thành công")
    void testCalculateDiscount_FreeShipping_Success() {
        when(couponRepository.findByCode("FREESHIP")).thenReturn(Optional.of(freeShippingCoupon));
        when(couponUsageRepository.countByUserIdAndCouponIdAndIsRefundedFalse(1L, 2L)).thenReturn(0L);

        CouponCalculationResult result = couponService.validateAndCalculateDiscount(
                1L, "FREESHIP", new BigDecimal("400000.00"), new BigDecimal("25000.00"), Collections.emptyList()
        );

        assertTrue(result.isValid());
        assertEquals(new BigDecimal("25000.00"), result.getDiscountAmount());
    }

    @Test
    @DisplayName("Áp mã thất bại khi coupon đã hết hạn")
    void testCalculateDiscount_CouponExpired() {
        percentageCoupon.setExpiresAt(LocalDateTime.now().minusDays(1));
        when(couponRepository.findByCode("MOCHUONG10")).thenReturn(Optional.of(percentageCoupon));

        CouponCalculationResult result = couponService.validateAndCalculateDiscount(
                1L, "MOCHUONG10", new BigDecimal("500000.00"), new BigDecimal("30000.00"), Collections.emptyList()
        );

        assertFalse(result.isValid());
        assertTrue(result.getMessage().contains("hết hạn sử dụng"));
        assertEquals(BigDecimal.ZERO, result.getDiscountAmount());
    }

    @Test
    @DisplayName("Áp mã thất bại khi đã hết lượt dùng tối đa trên toàn hệ thống")
    void testCalculateDiscount_MaxUsesExceeded() {
        percentageCoupon.setUsedCount(100); // max is 100
        when(couponRepository.findByCode("MOCHUONG10")).thenReturn(Optional.of(percentageCoupon));

        CouponCalculationResult result = couponService.validateAndCalculateDiscount(
                1L, "MOCHUONG10", new BigDecimal("500000.00"), new BigDecimal("30000.00"), Collections.emptyList()
        );

        assertFalse(result.isValid());
        assertTrue(result.getMessage().contains("hết lượt sử dụng trên hệ thống"));
    }

    @Test
    @DisplayName("Áp mã thất bại khi đơn hàng chưa đạt giá trị tối thiểu")
    void testCalculateDiscount_MinOrderValueNotMet() {
        when(couponRepository.findByCode("MOCHUONG10")).thenReturn(Optional.of(percentageCoupon));
        when(couponUsageRepository.countByUserIdAndCouponIdAndIsRefundedFalse(1L, 1L)).thenReturn(0L);

        CouponCalculationResult result = couponService.validateAndCalculateDiscount(
                1L, "MOCHUONG10", new BigDecimal("150000.00"), new BigDecimal("30000.00"), Collections.emptyList()
        );

        assertFalse(result.isValid());
        assertTrue(result.getMessage().contains("chưa đạt giá trị tối thiểu"));
    }

    @Test
    @DisplayName("Tạo mã giảm giá mới thất bại khi mã đã tồn tại")
    void testCreateCoupon_CodeExisted_ThrowsConflict() {
        CouponRequest request = CouponRequest.builder()
                .code("MOCHUONG10")
                .name("Giảm 10%")
                .type(Coupon.CouponType.PERCENTAGE)
                .value(new BigDecimal("10.00"))
                .startsAt(LocalDateTime.now())
                .build();

        when(couponRepository.existsByCode("MOCHUONG10")).thenReturn(true);

        AppException ex = assertThrows(AppException.class, () -> couponService.createCoupon(request));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }
}
