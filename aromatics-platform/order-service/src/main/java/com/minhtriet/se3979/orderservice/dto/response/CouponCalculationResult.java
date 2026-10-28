package com.minhtriet.se3979.orderservice.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    private BigDecimal finalTotal;
    private boolean isValid;
    private String message;

    @JsonProperty("valid")
    public boolean isValid() {
        return isValid;
    }
}
