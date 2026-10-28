package com.minhtriet.se3979.orderservice.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.minhtriet.se3979.orderservice.entity.Coupon.CouponType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class CouponRequest {

    @NotBlank(message = "Mã giảm giá không được để trống")
    private String code;

    @NotBlank(message = "Tên mã giảm giá không được để trống")
    @JsonAlias({"title"})
    private String name;

    @NotNull(message = "Loại mã giảm giá không được để trống")
    @JsonAlias({"discountType"})
    private CouponType type;

    @NotNull(message = "Giá trị giảm không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Giá trị giảm phải lớn hơn 0")
    @JsonAlias({"discountValue"})
    private BigDecimal value;

    private BigDecimal maxDiscountAmount;

    @Builder.Default
    private BigDecimal minOrderValue = BigDecimal.ZERO;

    @JsonAlias({"usageLimit"})
    private Integer maxUses;

    @Builder.Default
    private Integer maxUsesPerUser = 1;

    private List<Long> applicableProductIds;

    private List<Long> applicableCategoryIds;

    @NotNull(message = "Thời gian bắt đầu không được để trống")
    @JsonAlias({"startDate"})
    private LocalDateTime startsAt;

    @JsonAlias({"endDate"})
    private LocalDateTime expiresAt;

    @Builder.Default
    private Boolean isActive = true;
}
