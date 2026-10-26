package com.minhtriet.se3979.identityservice.service;

import com.minhtriet.se3979.identityservice.dto.request.ChangePasswordRequest;
import com.minhtriet.se3979.identityservice.dto.request.UpdateProfileRequest;
import com.minhtriet.se3979.identityservice.dto.response.UserResponse;
import com.minhtriet.se3979.identityservice.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    UserResponse getCurrentUserProfile(Long userId);

    UserResponse updateProfile(Long userId, UpdateProfileRequest request);

    void changePassword(Long userId, ChangePasswordRequest request);

    UserResponse getUserById(Long userId);

    Page<UserResponse> getAllUsers(String keyword, Role role, Boolean isActive, Pageable pageable);

    UserResponse updateUserRole(Long currentAdminId, Long targetUserId, Role newRole);

    UserResponse updateUserStatus(Long targetUserId, Boolean isActive);
}