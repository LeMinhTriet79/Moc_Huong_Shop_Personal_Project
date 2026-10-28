package com.minhtriet.se3979.orderservice.repository;

import com.minhtriet.se3979.orderservice.entity.OrderItem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByOrderId(Long orderId);

    interface TopProductProjection {
        Long getProductId();
        String getProductName();
        Long getTotalQuantity();
        BigDecimal getTotalRevenue();
    }

    @Query("SELECT oi.productId as productId, oi.productName as productName, " +
           "SUM(oi.quantity) as totalQuantity, SUM(oi.subtotal) as totalRevenue " +
           "FROM OrderItem oi " +
           "GROUP BY oi.productId, oi.productName " +
           "ORDER BY SUM(oi.quantity) DESC")
    List<TopProductProjection> findTopProducts(Pageable pageable);
}