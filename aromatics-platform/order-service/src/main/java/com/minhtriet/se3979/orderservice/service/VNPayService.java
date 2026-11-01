package com.minhtriet.se3979.orderservice.service;

import java.math.BigDecimal;
import java.util.Map;

public interface VNPayService {

    String createPaymentUrl(Long orderId, BigDecimal amount, String orderInfo, String ipAddress);

    Map<String, Object> processReturn(Map<String, String> requestParams);
}
