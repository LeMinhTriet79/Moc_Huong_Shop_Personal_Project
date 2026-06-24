package com.minhtriet.se3979.identityservice.repository;

import com.minhtriet.se3979.identityservice.entity.EmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {
    // Tìm thông tin xác thực dựa vào mã OTP/Token mà user gửi lên
    Optional<EmailVerification> findByToken(String token);
}