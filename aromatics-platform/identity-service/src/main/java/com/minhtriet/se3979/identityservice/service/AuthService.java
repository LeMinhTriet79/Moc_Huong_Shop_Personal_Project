package com.minhtriet.se3979.identityservice.service;

import com.minhtriet.se3979.identityservice.dto.request.GoogleAuthRequest;
import com.minhtriet.se3979.identityservice.dto.request.LoginRequest;
import com.minhtriet.se3979.identityservice.dto.request.RegisterRequest;
import com.minhtriet.se3979.identityservice.dto.response.AuthResponse;
import com.minhtriet.se3979.identityservice.dto.response.UserResponse;

public interface AuthService {
    UserResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request, String ipAddress, String deviceInfo);

    AuthResponse refreshToken(String refreshToken, String ipAddress, String deviceInfo);

    void logout(String refreshToken);

    void verifyEmail(String token);

    void forgotPassword(String email);

    void resetPassword(String token, String newPassword);

    AuthResponse googleLogin(GoogleAuthRequest request, String ipAddress, String deviceInfo);
}