package com.minhtriet.se3979.orderservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponCalculationResult {
    private String couponCode;
    private BigDecimal discountAmount;
    private boolean isValid;
    private String message;
}
