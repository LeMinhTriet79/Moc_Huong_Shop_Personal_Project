package com.minhtriet.se3979.catalogservice.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCancelledEvent {
    private Long orderId;
    private String orderCode;
    private Long userId;
    private String userEmail;
    private String cancelledReason;
    private String cancelledBy;
    private List<OrderItemEventDto> items;
    private LocalDateTime cancelledAt;
}
