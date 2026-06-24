package com.minhtriet.se3979.identityservice.service;

import com.minhtriet.se3979.identityservice.dto.request.LoginRequest;
import com.minhtriet.se3979.identityservice.dto.request.RegisterRequest;
import com.minhtriet.se3979.identityservice.dto.response.AuthResponse;
import com.minhtriet.se3979.identityservice.dto.response.UserResponse;

public interface AuthService {
    // Đăng ký tài khoản mới
    UserResponse register(RegisterRequest request);

    // Đăng nhập và nhận JWT
    AuthResponse login(LoginRequest request, String ipAddress, String deviceInfo);
}