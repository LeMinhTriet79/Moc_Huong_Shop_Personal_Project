package com.minhtriet.se3979.orderservice.dto.response;

import com.minhtriet.se3979.orderservice.entity.Transaction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {
    private Long id;
    private Long orderId;
    private String transactionCode;
    private String gatewayTransactionId;
    private String paymentMethod;
    private String type;
    private BigDecimal amount;
    private String status;
    private String gatewayResponseCode;
    private String gatewayResponseMessage;
    private String ipAddress;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;

    public static TransactionResponse fromEntity(Transaction t) {
        return TransactionResponse.builder()
                .id(t.getId())
                .orderId(t.getOrder() != null ? t.getOrder().getId() : null)
                .transactionCode(t.getTransactionCode())
                .gatewayTransactionId(t.getGatewayTransactionId())
                .paymentMethod(t.getPaymentMethod())
                .type(t.getType())
                .amount(t.getAmount())
                .status(t.getStatus())
                .gatewayResponseCode(t.getGatewayResponseCode())
                .gatewayResponseMessage(t.getGatewayResponseMessage())
                .ipAddress(t.getIpAddress())
                .createdAt(t.getCreatedAt())
                .completedAt(t.getCompletedAt())
                .build();
    }
}
