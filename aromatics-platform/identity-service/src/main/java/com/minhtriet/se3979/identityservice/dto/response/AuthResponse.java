package com.minhtriet.se3979.identityservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    @Builder.Default
    private String tokenType = "Bearer";
    private Long userId;
    private String email;
    private String fullName;
    private String role;
    private UserResponse user;

    public static AuthResponse of(String accessToken, String refreshToken, UserResponse user) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .userId(user != null ? user.getId() : null)
                .email(user != null ? user.getEmail() : null)
                .fullName(user != null ? user.getFullName() : null)
                .role(user != null ? user.getRole() : null)
                .user(user)
                .build();
    }
}
