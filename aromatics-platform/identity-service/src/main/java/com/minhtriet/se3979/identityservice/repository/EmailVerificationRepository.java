package com.minhtriet.se3979.identityservice.repository;

import com.minhtriet.se3979.identityservice.entity.EmailVerification;
import com.minhtriet.se3979.identityservice.enums.VerificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {
    Optional<EmailVerification> findByToken(String token);

    Optional<EmailVerification> findByTokenAndTypeAndIsUsedFalse(String token, VerificationType type);
}