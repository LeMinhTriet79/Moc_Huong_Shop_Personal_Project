package com.minhtriet.se3979.orderservice.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateOrderStatusRequest {

    @NotBlank(message = "Trạng thái mới không được để trống")
    @JsonAlias({"newStatus"})
    private String status;

    private String trackingNumber;

    private String carrier;

    private String note;
}
