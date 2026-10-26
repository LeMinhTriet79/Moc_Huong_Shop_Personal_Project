package com.minhtriet.se3979.identityservice.controller;

import com.minhtriet.se3979.identityservice.dto.request.ChangePasswordRequest;
import com.minhtriet.se3979.identityservice.dto.request.UpdateProfileRequest;
import com.minhtriet.se3979.identityservice.dto.response.ApiResponse;
import com.minhtriet.se3979.identityservice.dto.response.UserResponse;
import com.minhtriet.se3979.identityservice.security.SecurityUtils;
import com.minhtriet.se3979.identityservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/identity/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // API Lấy thông tin cá nhân: GET /api/identity/users/me
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser() {
        Long userId = SecurityUtils.getCurrentUserId();
        UserResponse profile = userService.getCurrentUserProfile(userId);
        return ResponseEntity.ok(ApiResponse.success(profile, "Lấy thông tin tài khoản thành công"));
    }

    // API Cập nhật thông tin cá nhân: PUT /api/identity/users/me
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        UserResponse updated = userService.updateProfile(userId, request);
        return ResponseEntity.ok(ApiResponse.success(updated, "Cập nhật hồ sơ thành công"));
    }

    // API Đổi mật khẩu: PUT /api/identity/users/me/password
    @PutMapping("/me/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        userService.changePassword(userId, request);
        return ResponseEntity.ok(ApiResponse.success(null, "Đổi mật khẩu thành công. Vui lòng đăng nhập lại!"));
    }

    // API Nội bộ: GET /api/identity/users/internal/{userId} (dùng cho Feign Client)
    @GetMapping("/internal/{userId}")
    public ResponseEntity<ApiResponse<UserResponse>> getInternalUser(@PathVariable Long userId) {
        UserResponse user = userService.getUserById(userId);
        return ResponseEntity.ok(ApiResponse.success(user, "Lấy thông tin nội bộ thành công"));
    }
}