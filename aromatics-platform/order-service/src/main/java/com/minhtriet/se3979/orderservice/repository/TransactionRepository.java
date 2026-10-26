package com.minhtriet.se3979.orderservice.repository;

import com.minhtriet.se3979.orderservice.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findByTransactionCode(String transactionCode);

    List<Transaction> findByOrderIdOrderByCreatedAtDesc(Long orderId);

    Optional<Transaction> findTopByOrderIdOrderByCreatedAtDesc(Long orderId);
}
