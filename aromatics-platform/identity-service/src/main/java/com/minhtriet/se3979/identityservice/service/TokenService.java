package com.minhtriet.se3979.identityservice.service;

import com.minhtriet.se3979.identityservice.entity.RefreshToken;
import com.minhtriet.se3979.identityservice.entity.User;

public interface TokenService {
    void saveRefreshToken(User user, String rawToken, String ipAddress, String deviceInfo);

    void revokeAllUserTokens(User user);

    void revokeToken(String rawToken);

    RefreshToken verifyAndRotateRefreshToken(String rawRefreshToken, String ipAddress, String deviceInfo);

    String hashToken(String rawToken);
}