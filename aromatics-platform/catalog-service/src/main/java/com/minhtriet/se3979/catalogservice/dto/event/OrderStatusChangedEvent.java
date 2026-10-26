package com.minhtriet.se3979.catalogservice.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusChangedEvent {
    private Long orderId;
    private String orderCode;
    private Long userId;
    private String userEmail;
    private String fromStatus;
    private String toStatus;
    private String note;
    private LocalDateTime changedAt;
}
