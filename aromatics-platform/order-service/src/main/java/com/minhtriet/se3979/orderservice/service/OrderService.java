package com.minhtriet.se3979.orderservice.service;

import com.minhtriet.se3979.orderservice.dto.request.CheckoutRequest;
import com.minhtriet.se3979.orderservice.entity.Order;
import org.springframework.transaction.annotation.Transactional;

public interface OrderService {
    @Transactional
    Order checkout(Long userId, CheckoutRequest request);
}
