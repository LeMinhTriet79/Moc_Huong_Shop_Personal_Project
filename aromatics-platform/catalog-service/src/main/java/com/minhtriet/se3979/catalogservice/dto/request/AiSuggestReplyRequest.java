package com.minhtriet.se3979.catalogservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiSuggestReplyRequest {

    private Long reviewId;

    private Integer rating; // 1 -> 5 sao

    @NotBlank(message = "Nội dung nhận xét của khách không được để trống")
    private String comment;

    private String customerName;

    private String productName;
}
