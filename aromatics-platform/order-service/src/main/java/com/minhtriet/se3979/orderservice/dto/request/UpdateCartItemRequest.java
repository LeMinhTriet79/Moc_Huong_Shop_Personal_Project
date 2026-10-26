package com.minhtriet.se3979.orderservice.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCartItemRequest {

    @NotNull(message = "Thiếu mã sản phẩm")
    private Long productId;

    @NotNull(message = "Thiếu mã biến thể")
    private Long variantId;

    @NotNull(message = "Thiếu số lượng mới")
    private Integer quantity;
}
