package com.minhtriet.se3979.orderservice.config;

import com.minhtriet.se3979.orderservice.entity.Coupon;
import com.minhtriet.se3979.orderservice.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final CouponRepository couponRepository;

    @Override
    public void run(String... args) {
        if (couponRepository.count() == 0) {
            log.info("🌱 Đang khởi tạo dữ liệu mẫu 3 mã giảm giá cho Mộc Hương Shop...");

            Coupon coupon10 = Coupon.builder()
                    .code("MOCHUONG10")
                    .name("Ưu đãi khai trương giảm 10%")
                    .type(Coupon.CouponType.PERCENTAGE)
                    .value(new BigDecimal("10.00"))
                    .maxDiscountAmount(new BigDecimal("50000.00"))
                    .minOrderValue(new BigDecimal("200000.00"))
                    .maxUses(1000)
                    .maxUsesPerUser(2)
                    .usedCount(0)
                    .isActive(true)
                    .startsAt(LocalDateTime.now().minusDays(1))
                    .expiresAt(LocalDateTime.now().plusMonths(6))
                    .build();

            Coupon freeShip = Coupon.builder()
                    .code("FREESHIP")
                    .name("Miễn phí vận chuyển toàn quốc")
                    .type(Coupon.CouponType.FREE_SHIPPING)
                    .value(new BigDecimal("30000.00"))
                    .maxDiscountAmount(new BigDecimal("30000.00"))
                    .minOrderValue(new BigDecimal("300000.00"))
                    .maxUses(500)
                    .maxUsesPerUser(1)
                    .usedCount(0)
                    .isActive(true)
                    .startsAt(LocalDateTime.now().minusDays(1))
                    .expiresAt(LocalDateTime.now().plusMonths(6))
                    .build();

            Coupon tramHuong50k = Coupon.builder()
                    .code("TRAMHUONG50K")
                    .name("Giảm 50.000đ cho đơn hàng Trầm Hương từ 500k")
                    .type(Coupon.CouponType.FIXED_AMOUNT)
                    .value(new BigDecimal("50000.00"))
                    .maxDiscountAmount(new BigDecimal("50000.00"))
                    .minOrderValue(new BigDecimal("500000.00"))
                    .maxUses(200)
                    .maxUsesPerUser(1)
                    .usedCount(0)
                    .isActive(true)
                    .startsAt(LocalDateTime.now().minusDays(1))
                    .expiresAt(LocalDateTime.now().plusMonths(3))
                    .build();

            couponRepository.save(coupon10);
            couponRepository.save(freeShip);
            couponRepository.save(tramHuong50k);

            log.info("✅ Đã khởi tạo thành công 3 mã ưu đãi mẫu: MOCHUONG10, FREESHIP, TRAMHUONG50K");
        }
    }
}
