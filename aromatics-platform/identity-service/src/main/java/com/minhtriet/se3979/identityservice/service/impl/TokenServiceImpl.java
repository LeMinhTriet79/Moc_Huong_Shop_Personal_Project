package com.minhtriet.se3979.identityservice.service.impl;

import com.minhtriet.se3979.identityservice.entity.RefreshToken;
import com.minhtriet.se3979.identityservice.entity.User;
import com.minhtriet.se3979.identityservice.exception.AppException;
import com.minhtriet.se3979.identityservice.repository.RefreshTokenRepository;
import com.minhtriet.se3979.identityservice.service.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Base64;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh-expiration:604800000}")
    private long refreshExpiration;

    @Override
    @Transactional
    public void saveRefreshToken(User user, String rawToken, String ipAddress, String deviceInfo) {
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .tokenHash(hashToken(rawToken))
                .ipAddress(ipAddress)
                .deviceInfo(deviceInfo)
                .expiresAt(LocalDateTime.now().plusSeconds(refreshExpiration / 1000))
                .isRevoked(false)
                .build();

        refreshTokenRepository.save(refreshToken);
    }

    @Override
    @Transactional
    public void revokeAllUserTokens(User user) {
        if (user != null && user.getId() != null) {
            refreshTokenRepository.revokeAllByUserId(user.getId());
            log.info("Đã thu hồi tất cả Refresh Token của user id {}", user.getId());
        }
    }

    @Override
    @Transactional
    public void revokeToken(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) return;
        String hash = hashToken(rawToken);
        refreshTokenRepository.findByTokenHash(hash).ifPresent(token -> {
            token.setIsRevoked(true);
            refreshTokenRepository.save(token);
            log.info("Đã thu hồi token id {}", token.getId());
        });
    }

    @Override
    @Transactional
    public RefreshToken verifyAndRotateRefreshToken(String rawRefreshToken, String ipAddress, String deviceInfo) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Refresh token không được để trống");
        }

        String hash = hashToken(rawRefreshToken);
        RefreshToken storedToken = refreshTokenRepository.findByTokenHash(hash)
                .orElseThrow(() -> new AppException(HttpStatus.UNAUTHORIZED, "Refresh token không tồn tại hoặc không hợp lệ"));

        if (Boolean.TRUE.equals(storedToken.getIsRevoked())) {
            // Nghi vấn bị đánh cắp token -> thu hồi toàn bộ token của user này để bảo vệ tài khoản
            revokeAllUserTokens(storedToken.getUser());
            throw new AppException(HttpStatus.UNAUTHORIZED, "Refresh token đã bị thu hồi trước đó. Vui lòng đăng nhập lại!");
        }

        if (storedToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            storedToken.setIsRevoked(true);
            refreshTokenRepository.save(storedToken);
            throw new AppException(HttpStatus.UNAUTHORIZED, "Refresh token đã hết hạn. Vui lòng đăng nhập lại!");
        }

        // Đánh dấu token cũ là đã revoke (Rotation)
        storedToken.setIsRevoked(true);
        refreshTokenRepository.save(storedToken);

        return storedToken;
    }

    @Override
    public String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Lỗi băm token", e);
        }
    }
}