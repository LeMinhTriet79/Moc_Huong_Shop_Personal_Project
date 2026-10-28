package com.minhtriet.se3979.orderservice.controller;

import com.minhtriet.se3979.orderservice.dto.request.CheckoutRequest;
import com.minhtriet.se3979.orderservice.dto.request.ReturnOrderRequest;
import com.minhtriet.se3979.orderservice.dto.response.ApiResponse;
import com.minhtriet.se3979.orderservice.dto.response.OrderDetailResponse;
import com.minhtriet.se3979.orderservice.dto.response.OrderResponse;
import com.minhtriet.se3979.orderservice.security.SecurityUtils;
import com.minhtriet.se3979.orderservice.service.OrderExportService;
import com.minhtriet.se3979.orderservice.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/order/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final OrderExportService orderExportService;

    // 1. Đặt hàng (Checkout)
    @PostMapping("/checkout")
    public ResponseEntity<ApiResponse<OrderResponse>> checkout(@Valid @RequestBody CheckoutRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        OrderResponse response = orderService.checkout(userId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Đặt hàng thành công"));
    }

    // 2. Xem danh sách đơn hàng của khách hàng hiện tại
    @GetMapping
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getMyOrders(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = SecurityUtils.getCurrentUserId();
        Page<OrderResponse> orders = orderService.getCustomerOrders(
                userId, status, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
        return ResponseEntity.ok(ApiResponse.success(orders, "Lấy danh sách đơn hàng thành công"));
    }

    // 3. Xem chi tiết đơn hàng theo orderCode
    @GetMapping("/{orderCode}")
    public ResponseEntity<ApiResponse<OrderDetailResponse>> getOrderDetail(@PathVariable String orderCode) {
        Long userId = SecurityUtils.getCurrentUserId();
        OrderDetailResponse detail = orderService.getOrderDetailByCode(orderCode, userId);
        return ResponseEntity.ok(ApiResponse.success(detail, "Lấy chi tiết đơn hàng thành công"));
    }

    // 4. Hủy đơn hàng (Khi đang PENDING_PAYMENT, CONFIRMED, hoặc PREPARING)
    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(
            @PathVariable Long orderId,
            @RequestParam(required = false) String reason,
            @RequestBody(required = false) Map<String, String> body) {
        String finalReason = reason;
        if ((finalReason == null || finalReason.isBlank()) && body != null) {
            finalReason = body.get("reason");
        }
        if (finalReason == null || finalReason.isBlank()) {
            finalReason = "Khách hàng hủy đơn";
        }
        OrderResponse response = orderService.cancelOrder(orderId, finalReason);
        return ResponseEntity.ok(ApiResponse.success(response, "Hủy đơn hàng thành công"));
    }

    // 5. Yêu cầu trả hàng / hoàn tiền (Khi đơn đã DELIVERED)
    @PostMapping("/{orderId}/return-request")
    public ResponseEntity<ApiResponse<OrderResponse>> requestReturn(
            @PathVariable Long orderId,
            @RequestBody(required = false) ReturnOrderRequest request,
            @RequestParam(required = false) String reason) {
        Long userId = SecurityUtils.getCurrentUserId();
        String finalReason = reason;
        if (request != null && request.getReason() != null && !request.getReason().isBlank()) {
            finalReason = request.getReason();
        }
        if (finalReason == null || finalReason.isBlank()) {
            finalReason = "Khách hàng yêu cầu trả hàng/hoàn tiền";
        }
        OrderResponse response = orderService.requestReturn(orderId, userId, finalReason);
        return ResponseEntity.ok(ApiResponse.success(response, "Đã gửi yêu cầu trả hàng/hoàn tiền"));
    }

    // 6. Xuất hóa đơn bán hàng PDF (iText7)
    @GetMapping("/{orderId}/invoice-pdf")
    public ResponseEntity<byte[]> downloadInvoicePdf(@PathVariable Long orderId) {
        Long userId = SecurityUtils.getCurrentUserId();
        String role = SecurityUtils.getCurrentUserRole();
        byte[] pdfBytes = orderExportService.generateInvoicePdf(orderId, userId, role);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=invoice-" + orderId + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }
}