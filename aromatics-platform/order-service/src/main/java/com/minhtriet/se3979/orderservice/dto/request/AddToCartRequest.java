package com.minhtriet.se3979.orderservice.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AddToCartRequest {
    @NotNull(message = "Thiếu mã sản phẩm")
    private Long productId;

    @NotNull(message = "Thiếu mã biến thể")
    private Long variantId;

    @Min(value = 1, message = "Số lượng ít nhất là 1")
    private int quantity;

    // Các trường dưới đây do Frontend gửi lên tạm để hiển thị cho nhanh (sẽ check lại giá khi thanh toán)
    private String productName;
    private String variantName;
    private String productImageUrl;
    private BigDecimal price;
}