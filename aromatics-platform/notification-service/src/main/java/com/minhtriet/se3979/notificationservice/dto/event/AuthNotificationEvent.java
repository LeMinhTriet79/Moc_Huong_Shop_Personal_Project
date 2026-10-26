package com.minhtriet.se3979.notificationservice.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthNotificationEvent {
    private Long userId;
    private String email;
    private String fullName;
    private String type; // VERIFY_EMAIL, RESET_PASSWORD
    private String token;
    private LocalDateTime expiresAt;
}
