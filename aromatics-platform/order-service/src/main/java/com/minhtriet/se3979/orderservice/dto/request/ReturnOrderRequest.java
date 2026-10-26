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
public class ReturnOrderRequest {

    @NotBlank(message = "Lý do trả hàng không được để trống")
    private String reason;
}
