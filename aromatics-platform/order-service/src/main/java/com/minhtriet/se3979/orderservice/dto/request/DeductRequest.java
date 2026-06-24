package com.minhtriet.se3979.orderservice.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeductRequest {
    private Long variantId;
    private int quantity;
}