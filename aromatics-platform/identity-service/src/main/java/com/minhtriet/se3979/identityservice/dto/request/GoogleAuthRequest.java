package com.minhtriet.se3979.identityservice.dto.request;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoogleAuthRequest {
    private String idToken;

    @Email(message = "Email không đúng định dạng")
    private String email;

    private String fullName;

    private String avatarUrl;

    private String providerId;
}
