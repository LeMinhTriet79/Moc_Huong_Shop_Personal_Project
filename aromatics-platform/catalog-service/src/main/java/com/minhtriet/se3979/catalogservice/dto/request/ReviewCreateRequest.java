package com.minhtriet.se3979.catalogservice.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReviewCreateRequest {

    @NotNull(message = "Thiếu ID của biến thể sản phẩm")
    private Long variantId;

    @NotNull(message = "Thiếu mã chi tiết đơn hàng (để kiểm tra đã mua hay chưa)")
    private Long orderItemId;

    @NotNull(message = "Vui lòng chọn số sao")
    @Min(value = 1, message = "Đánh giá thấp nhất là 1 sao")
    @Max(value = 5, message = "Đánh giá cao nhất là 5 sao")
    private Integer rating;

    private String title;

    private String content;
}