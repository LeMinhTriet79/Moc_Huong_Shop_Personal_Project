package com.minhtriet.se3979.catalogservice.dto.request;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryUpdateRequest {

    @Min(value = 0, message = "Số lượng tồn kho không được âm")
    private Integer quantity;

    @Min(value = 0, message = "Ngưỡng cảnh báo sắp hết hàng không được âm")
    private Integer lowStockThreshold;
}
