package com.minhtriet.se3979.identityservice.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class UserResponse {
    private Long id;
    private String email;
    private String fullName;
    private String phoneNumber;
    private String avatarUrl;
    private String role;
    private Boolean isEmailVerified;
    private LocalDate dateOfBirth;
    private LocalDateTime createdAt;
}