package com.minhtriet.se3979.orderservice.controller;

import com.minhtriet.se3979.orderservice.dto.request.UpdateOrderStatusRequest;
import com.minhtriet.se3979.orderservice.dto.request.UpdateStaffNotesRequest;
import com.minhtriet.se3979.orderservice.dto.response.ApiResponse;
import com.minhtriet.se3979.orderservice.dto.response.DashboardStatsResponse;
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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/order/admin/orders")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
public class AdminOrderController {

    private final OrderService orderService;
    private final OrderExportService orderExportService;

    // 1. Phân trang & Lọc toàn bộ đơn hàng
    @GetMapping
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getAllOrders(
            @RequestParam(required = false) String orderCode,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String paymentMethod,
            @RequestParam(required = false) String paymentStatus,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        String effectiveCode = (orderCode != null && !orderCode.isBlank()) ? orderCode : keyword;

        LocalDateTime effectiveFrom = fromDate;
        if (effectiveFrom == null && startDate != null && !startDate.isBlank()) {
            try {
                effectiveFrom = LocalDate.parse(startDate).atStartOfDay();
            } catch (Exception ignored) {}
        }

        LocalDateTime effectiveTo = toDate;
        if (effectiveTo == null && endDate != null && !endDate.isBlank()) {
            try {
                effectiveTo = LocalDate.parse(endDate).atTime(23, 59, 59);
            } catch (Exception ignored) {}
        }

        Page<OrderResponse> orders = orderService.getAllOrdersAdmin(
                effectiveCode, status, paymentMethod, paymentStatus, effectiveFrom, effectiveTo,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));

        return ResponseEntity.ok(ApiResponse.success(orders, "Lấy danh sách đơn hàng thành công"));
    }

    // 2. Xem chi tiết đơn hàng đầy đủ
    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderDetailResponse>> getOrderDetail(@PathVariable Long orderId) {
        OrderDetailResponse detail = orderService.getOrderDetailById(orderId);
        return ResponseEntity.ok(ApiResponse.success(detail, "Lấy chi tiết đơn hàng thành công"));
    }

    // 3. Cập nhật trạng thái đơn hàng theo State Machine
    @PutMapping("/{orderId}/status")
    public ResponseEntity<ApiResponse<OrderDetailResponse>> updateOrderStatus(
            @PathVariable Long orderId,
            @Valid @RequestBody UpdateOrderStatusRequest request) {

        String role = SecurityUtils.getCurrentUserRole();
        Long userId = SecurityUtils.getCurrentUserId();

        OrderDetailResponse response = orderService.updateOrderStatus(
                orderId, request.getStatus(), request.getNote(), role, userId,
                request.getTrackingNumber(), request.getCarrier());

        return ResponseEntity.ok(ApiResponse.success(response, "Cập nhật trạng thái đơn hàng thành công"));
    }

    // 4. Cập nhật ghi chú nội bộ (staffNotes)
    @PutMapping("/{orderId}/staff-notes")
    public ResponseEntity<ApiResponse<String>> updateStaffNotes(
            @PathVariable Long orderId,
            @RequestBody UpdateStaffNotesRequest request) {

        orderService.updateStaffNotes(orderId, request.getStaffNotes());
        return ResponseEntity.ok(ApiResponse.success("OK", "Cập nhật ghi chú nội bộ thành công"));
    }

    // 5. Xuất báo cáo danh sách đơn hàng ra file Excel (Apache POI)
    @GetMapping("/export-excel")
    public ResponseEntity<byte[]> exportOrdersToExcel(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String status) {

        LocalDate effectiveFrom = fromDate;
        if (effectiveFrom == null && startDate != null && !startDate.isBlank()) {
            try {
                effectiveFrom = LocalDate.parse(startDate);
            } catch (Exception ignored) {}
        }

        LocalDate effectiveTo = toDate;
        if (effectiveTo == null && endDate != null && !endDate.isBlank()) {
            try {
                effectiveTo = LocalDate.parse(endDate);
            } catch (Exception ignored) {}
        }

        byte[] excelBytes = orderExportService.exportOrdersToExcel(effectiveFrom, effectiveTo, status);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=orders-report.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelBytes);
    }

    // 6. Thống kê Dashboard
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> getOrderStatistics() {
        DashboardStatsResponse stats = orderExportService.getOrderStatistics();
        return ResponseEntity.ok(ApiResponse.success(stats, "Lấy dữ liệu thống kê thành công"));
    }
}
