package com.minhtriet.se3979.orderservice.dto.response;

import com.minhtriet.se3979.orderservice.entity.OrderStatusHistory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusHistoryResponse {
    private Long id;
    private Long orderId;
    private String fromStatus;
    private String toStatus;
    private String changedByType;
    private Long changedById;
    private String note;
    private LocalDateTime createdAt;

    public static OrderStatusHistoryResponse fromEntity(OrderStatusHistory h) {
        return OrderStatusHistoryResponse.builder()
                .id(h.getId())
                .orderId(h.getOrder() != null ? h.getOrder().getId() : null)
                .fromStatus(h.getFromStatus())
                .toStatus(h.getToStatus())
                .changedByType(h.getChangedByType())
                .changedById(h.getChangedById())
                .note(h.getNote())
                .createdAt(h.getCreatedAt())
                .build();
    }
}
