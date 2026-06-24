package com.minhtriet.se3979.catalogservice.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DeductRequest {
    @NotNull(message = "Thiếu ID biến thể")
    private Long variantId;

    @Min(value = 1, message = "Số lượng phải lớn hơn 0")
    private int quantity;
}