package com.minhtriet.se3979.orderservice.service;

import com.minhtriet.se3979.orderservice.dto.response.DashboardStatsResponse;

import java.time.LocalDate;

public interface OrderExportService {

    byte[] generateInvoicePdf(Long orderId, Long currentUserId, String currentRole);

    byte[] exportOrdersToExcel(LocalDate fromDate, LocalDate toDate);

    DashboardStatsResponse getOrderStatistics();
}
