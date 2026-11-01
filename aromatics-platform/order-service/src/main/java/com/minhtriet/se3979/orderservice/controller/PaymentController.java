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
import java.util.Map;

@Slf4j
@RestController
@RequestMapping({"/api/order/payment", "/api/order/payments"})
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

    // 3. API Nhận Kết Quả Trả Về Từ VNPay Callback (Hỗ trợ cả IPN lẫn User Redirect Return)
    @GetMapping("/vnpay-return")
    public ResponseEntity<ApiResponse<Map<String, Object>>> vnpayReturn(@RequestParam Map<String, String> allParams) {
        Map<String, Object> result = vnPayService.processReturn(allParams);
        boolean isSuccess = Boolean.TRUE.equals(result.get("success"));
        String message = (String) result.getOrDefault("message", isSuccess ? "Xác thực thanh toán VNPay thành công" : "Giao dịch VNPay không thành công hoặc chữ ký không hợp lệ");
        return ResponseEntity.ok(ApiResponse.success(result, message));
    }
}
