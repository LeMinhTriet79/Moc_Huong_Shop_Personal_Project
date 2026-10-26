package com.minhtriet.se3979.identityservice.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.minhtriet.se3979.identityservice.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponse {
    private Long id;
    private String email;
    private String fullName;
    private String phoneNumber;
    private String avatarUrl;
    private String role;
    private Boolean isActive;
    private Boolean isEmailVerified;
    private LocalDate dateOfBirth;
    private LocalDateTime createdAt;
    private List<AddressResponse> addresses;

    public static UserResponse fromEntity(User user) {
        if (user == null) return null;
        List<AddressResponse> addressResponses = user.getAddresses() != null
                ? user.getAddresses().stream().map(AddressResponse::fromEntity).toList()
                : Collections.emptyList();

        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phoneNumber(user.getPhoneNumber())
                .avatarUrl(user.getAvatarUrl())
                .role(user.getRole() != null ? user.getRole().name() : null)
                .isActive(user.getIsActive())
                .isEmailVerified(user.getIsEmailVerified())
                .dateOfBirth(user.getDateOfBirth())
                .createdAt(user.getCreatedAt())
                .addresses(addressResponses.isEmpty() ? null : addressResponses)
                .build();
    }
}