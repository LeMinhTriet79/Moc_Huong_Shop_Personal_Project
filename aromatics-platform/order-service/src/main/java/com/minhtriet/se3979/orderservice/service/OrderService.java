package com.minhtriet.se3979.orderservice.service;

import com.minhtriet.se3979.orderservice.dto.request.CheckoutRequest;
import com.minhtriet.se3979.orderservice.dto.response.OrderDetailResponse;
import com.minhtriet.se3979.orderservice.dto.response.OrderResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

public interface OrderService {

    OrderResponse checkout(Long userId, CheckoutRequest request);

    OrderResponse cancelOrder(Long orderId, String reason);

    OrderResponse requestReturn(Long orderId, Long userId, String reason);

    Page<OrderResponse> getCustomerOrders(Long userId, String status, Pageable pageable);

    OrderDetailResponse getOrderDetailByCode(String orderCode, Long userId);

    OrderDetailResponse getOrderDetailById(Long orderId);

    Page<OrderResponse> getAllOrdersAdmin(
            String orderCode, String status, String paymentMethod, String paymentStatus,
            LocalDateTime fromDate, LocalDateTime toDate, Pageable pageable);

    OrderDetailResponse updateOrderStatus(
            Long orderId, String newStatus, String note, String changedByType, Long changedById);

    OrderDetailResponse updateOrderStatus(
            Long orderId, String newStatus, String note, String changedByType, Long changedById,
            String trackingNumber, String carrier);

    void updateStaffNotes(Long orderId, String staffNotes);

    void handlePaymentSuccess(
            String orderCode, String transactionCode, String gatewayTxnId,
            BigDecimal amount, String responseCode, String message, Map<String, Object> rawResponse);

    void handlePaymentFailure(
            String orderCode, String transactionCode, String gatewayTxnId,
            BigDecimal amount, String responseCode, String message, Map<String, Object> rawResponse);
}
