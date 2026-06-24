package com.minhtriet.se3979.identityservice.service.impl;

import com.minhtriet.se3979.identityservice.entity.RefreshToken;
import com.minhtriet.se3979.identityservice.entity.User;
import com.minhtriet.se3979.identityservice.repository.RefreshTokenRepository;
import com.minhtriet.se3979.identityservice.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh-expiration}") // Thêm jwt.refresh-expiration: 604800000 (7 ngày) vào application.yml nhé
    private long refreshExpiration;

    @Override
    public void saveRefreshToken(User user, String rawToken, String ipAddress, String deviceInfo) {
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .tokenHash(hashToken(rawToken)) // Lưu Hash chứ không lưu chuỗi gốc
                .ipAddress(ipAddress)
                .deviceInfo(deviceInfo)
                .expiresAt(LocalDateTime.now().plusSeconds(refreshExpiration / 1000))
                .isRevoked(false)
                .build();

        refreshTokenRepository.save(refreshToken);
    }

    @Override
    public void revokeAllUserTokens(User user) {
        // Trong hệ thống thực tế, bạn sẽ query các token chưa revoke của user và update isRevoked = true
        // Tạm thời để đơn giản hóa logic MVP
    }

    // Tiện ích băm mã token bằng SHA-256
    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Lỗi băm token", e);
        }
    }
}