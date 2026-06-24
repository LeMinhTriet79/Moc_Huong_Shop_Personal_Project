package com.minhtriet.se3979.identityservice.service;

import com.minhtriet.se3979.identityservice.entity.User;

public interface TokenService {
    // Lưu một Refresh Token mới vào database (dạng hash)
    void saveRefreshToken(User user, String rawToken, String ipAddress, String deviceInfo);

    // Thu hồi (Revoke) tất cả các token đang hoạt động của 1 user
    void revokeAllUserTokens(User user);
}