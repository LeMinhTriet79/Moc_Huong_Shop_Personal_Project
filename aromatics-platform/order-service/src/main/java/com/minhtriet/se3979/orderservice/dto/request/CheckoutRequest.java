package com.minhtriet.se3979.orderservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutRequest {

    @NotNull(message = "Địa chỉ nhận hàng không được để trống")
    private Map<String, Object> shippingAddress;

    @NotBlank(message = "Phương thức thanh toán không được để trống")
    private String paymentMethod; // VNPAY, MOMO, COD

    private String couponCode;

    private String notes;

    @NotBlank(message = "idempotencyKey không được để trống")
    private String idempotencyKey;
}