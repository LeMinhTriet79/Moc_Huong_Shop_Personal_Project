package com.minhtriet.se3979.orderservice.controller;

import com.minhtriet.se3979.orderservice.dto.response.ApiResponse;
import com.minhtriet.se3979.orderservice.service.VNPayService;
import com.minhtriet.se3979.orderservice.vnpay.VNPayUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/order/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final VNPayService vnPayService;

    // 1. API Tạo Link Thanh Toán theo orderId
    @GetMapping("/create-url/{orderId}")
    public ResponseEntity<ApiResponse<Map<String, String>>> createPaymentUrl(
            @PathVariable Long orderId, HttpServletRequest request) {
        String ipAddress = VNPayUtil.getIpAddress(request);
        String paymentUrl = vnPayService.createPaymentUrl(orderId, null, null, ipAddress);
        return ResponseEntity.ok(ApiResponse.success(
                Map.of("paymentUrl", paymentUrl), "Tạo link thanh toán VNPay thành công"));
    }

    // 2. API Tạo Link Thanh Toán linh hoạt
    @GetMapping("/create-payment")
    public ResponseEntity<ApiResponse<Map<String, String>>> createPayment(
            @RequestParam(required = false) Long orderId,
            @RequestParam(required = false) BigDecimal amount,
            @RequestParam(required = false) String orderInfo,
            HttpServletRequest request) {
        String ipAddress = VNPayUtil.getIpAddress(request);
        String paymentUrl = vnPayService.createPaymentUrl(orderId, amount, orderInfo, ipAddress);
        return ResponseEntity.ok(ApiResponse.success(
                Map.of("paymentUrl", paymentUrl), "Tạo link thanh toán VNPay thành công"));
    }

    // 3. API Nhận Kết Quả Trả Về Từ VNPay Callback
    @GetMapping("/vnpay-return")
    public ResponseEntity<ApiResponse<String>> vnpayReturn(HttpServletRequest request) {
        Map<String, String> requestParams = new HashMap<>();
        for (Enumeration<String> params = request.getParameterNames(); params.hasMoreElements(); ) {
            String paramName = params.nextElement();
            String paramValue = request.getParameter(paramName);
            if (paramValue != null && !paramValue.isEmpty()) {
                requestParams.put(paramName, paramValue);
            }
        }

        Map<String, Object> result = vnPayService.processReturn(requestParams);
        boolean isSuccess = Boolean.TRUE.equals(result.get("success"));
        String message = (String) result.get("message");

        if (isSuccess) {
            return ResponseEntity.ok(ApiResponse.success(message, "Thanh toán thành công"));
        } else {
            return ResponseEntity.badRequest().body(ApiResponse.error(message));
        }
    }
}
