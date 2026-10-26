package com.minhtriet.se3979.orderservice.dto.response;

import com.minhtriet.se3979.orderservice.entity.Coupon;
import com.minhtriet.se3979.orderservice.entity.Coupon.CouponType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponResponse {
    private Long id;
    private String code;
    private String name;
    private CouponType type;
    private BigDecimal value;
    private BigDecimal maxDiscountAmount;
    private BigDecimal minOrderValue;
    private Integer maxUses;
    private Integer maxUsesPerUser;
    private Integer usedCount;
    private List<Long> applicableProductIds;
    private List<Long> applicableCategoryIds;
    private LocalDateTime startsAt;
    private LocalDateTime expiresAt;
    private Boolean isActive;
    private LocalDateTime createdAt;

    public static CouponResponse fromEntity(Coupon c) {
        return CouponResponse.builder()
                .id(c.getId())
                .code(c.getCode())
                .name(c.getName())
                .type(c.getType())
                .value(c.getValue())
                .maxDiscountAmount(c.getMaxDiscountAmount())
                .minOrderValue(c.getMinOrderValue())
                .maxUses(c.getMaxUses())
                .maxUsesPerUser(c.getMaxUsesPerUser())
                .usedCount(c.getUsedCount())
                .applicableProductIds(c.getApplicableProductIds())
                .applicableCategoryIds(c.getApplicableCategoryIds())
                .startsAt(c.getStartsAt())
                .expiresAt(c.getExpiresAt())
                .isActive(c.getIsActive())
                .createdAt(c.getCreatedAt())
                .build();
    }
}
