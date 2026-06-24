package com.minhtriet.se3979.orderservice.repository;

import com.minhtriet.se3979.orderservice.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}