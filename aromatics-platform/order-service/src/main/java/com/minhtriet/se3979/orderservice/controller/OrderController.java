package com.minhtriet.se3979.orderservice.controller;

import com.minhtriet.se3979.orderservice.dto.request.CheckoutRequest;
import com.minhtriet.se3979.orderservice.entity.Order;
import com.minhtriet.se3979.orderservice.security.JwtUtil;
import com.minhtriet.se3979.orderservice.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/order/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final JwtUtil jwtUtil;

    // Hàm tiện ích: Lấy ID người dùng từ Token JWT
    private Long getUserIdFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Không tìm thấy Token bảo mật");
        }
        String token = authHeader.substring(7);
        return jwtUtil.extractUserId(token);
    }

    // API: POST /api/order/orders/checkout
    @PostMapping("/checkout")
    public ResponseEntity<Order> checkout(@Valid @RequestBody CheckoutRequest request, HttpServletRequest httpRequest) {
        Long userId = getUserIdFromRequest(httpRequest);

        // Gọi Service xử lý luồng đặt hàng (Trừ kho -> Lưu DB -> Xóa giỏ hàng)
        Order order = orderService.checkout(userId, request);

        return ResponseEntity.ok(order);
    }

    // API: PUT /api/order/orders/{orderId}/cancel
    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<Order> cancelOrder(@PathVariable Long orderId, HttpServletRequest httpRequest) {
        Long userId = getUserIdFromRequest(httpRequest);
        Order order = orderService.cancelOrder(userId, orderId);
        return ResponseEntity.ok(order);
    }
}