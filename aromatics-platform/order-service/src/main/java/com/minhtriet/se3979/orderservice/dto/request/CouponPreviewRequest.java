package com.minhtriet.se3979.orderservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponPreviewRequest {

    @NotBlank(message = "Mã giảm giá không được để trống")
    private String couponCode;

    private java.math.BigDecimal subtotal;

    private java.math.BigDecimal shippingFee;
}
