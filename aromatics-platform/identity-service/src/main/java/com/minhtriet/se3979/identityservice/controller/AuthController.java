package com.minhtriet.se3979.identityservice.controller;

import com.minhtriet.se3979.identityservice.dto.request.*;
import com.minhtriet.se3979.identityservice.dto.response.ApiResponse;
import com.minhtriet.se3979.identityservice.dto.response.AuthResponse;
import com.minhtriet.se3979.identityservice.dto.response.UserResponse;
import com.minhtriet.se3979.identityservice.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/identity/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // API Đăng ký: POST /api/identity/auth/register
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse response = authService.register(request);
        return new ResponseEntity<>(ApiResponse.success(response, "Đăng ký tài khoản thành công"), HttpStatus.CREATED);
    }

    // API Đăng nhập: POST /api/identity/auth/login
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        String ipAddress = httpRequest.getRemoteAddr();
        String deviceInfo = httpRequest.getHeader("User-Agent");
        AuthResponse response = authService.login(request, ipAddress, deviceInfo);
        return ResponseEntity.ok(ApiResponse.success(response, "Đăng nhập thành công"));
    }

    // API Làm mới token: POST /api/identity/auth/refresh
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(@Valid @RequestBody RefreshTokenRequest request, HttpServletRequest httpRequest) {
        String ipAddress = httpRequest.getRemoteAddr();
        String deviceInfo = httpRequest.getHeader("User-Agent");
        AuthResponse response = authService.refreshToken(request.getRefreshToken(), ipAddress, deviceInfo);
        return ResponseEntity.ok(ApiResponse.success(response, "Làm mới phiên đăng nhập thành công"));
    }

    // API Đăng xuất: POST /api/identity/auth/logout
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@Valid @RequestBody RefreshTokenRequest request) {
        authService.logout(request.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.success(null, "Đăng xuất thành công"));
    }

    // API Xác thực email (qua query param GET hoặc POST): GET /api/identity/auth/verify-email?token=...
    @GetMapping("/verify-email")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(@RequestParam("token") String token) {
        authService.verifyEmail(token);
        return ResponseEntity.ok(ApiResponse.success(null, "Xác thực email thành công! Tài khoản của bạn đã được kích hoạt."));
    }

    // API Quên mật khẩu: POST /api/identity/auth/forgot-password
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request.getEmail());
        return ResponseEntity.ok(ApiResponse.success(null, "Hướng dẫn đặt lại mật khẩu đã được gửi đến email của bạn."));
    }

    // API Đặt lại mật khẩu: POST /api/identity/auth/reset-password
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok(ApiResponse.success(null, "Đặt lại mật khẩu thành công. Vui lòng đăng nhập bằng mật khẩu mới!"));
    }

    // API Đăng nhập Google: POST /api/identity/auth/google
    @PostMapping("/google")
    public ResponseEntity<ApiResponse<AuthResponse>> googleLogin(@Valid @RequestBody GoogleAuthRequest request, HttpServletRequest httpRequest) {
        String ipAddress = httpRequest.getRemoteAddr();
        String deviceInfo = httpRequest.getHeader("User-Agent");
        AuthResponse response = authService.googleLogin(request, ipAddress, deviceInfo);
        return ResponseEntity.ok(ApiResponse.success(response, "Đăng nhập bằng Google thành công"));
    }
}