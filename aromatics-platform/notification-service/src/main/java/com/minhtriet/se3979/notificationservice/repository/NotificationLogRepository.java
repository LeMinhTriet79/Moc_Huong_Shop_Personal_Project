package com.minhtriet.se3979.notificationservice.repository;

import com.minhtriet.se3979.notificationservice.entity.NotificationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {

    boolean existsByIdempotencyKey(String idempotencyKey);

    Optional<NotificationLog> findByIdempotencyKey(String idempotencyKey);

    List<NotificationLog> findByStatusAndRetryCountLessThanAndNextRetryAtLessThanEqual(
            String status, Integer maxRetry, LocalDateTime now);
}
