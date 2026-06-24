package com.minhtriet.se3979.orderservice.repository;

import com.minhtriet.se3979.orderservice.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}