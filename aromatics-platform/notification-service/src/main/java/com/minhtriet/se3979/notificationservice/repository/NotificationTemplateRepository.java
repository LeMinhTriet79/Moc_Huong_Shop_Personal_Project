package com.minhtriet.se3979.notificationservice.repository;

import com.minhtriet.se3979.notificationservice.entity.NotificationTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, Long> {

    Optional<NotificationTemplate> findByTemplateKeyAndIsActiveTrue(String templateKey);

    Optional<NotificationTemplate> findByEventTypeAndChannelAndIsActiveTrue(String eventType, String channel);
}
