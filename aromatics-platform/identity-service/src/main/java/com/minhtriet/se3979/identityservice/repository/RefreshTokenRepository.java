package com.minhtriet.se3979.identityservice.repository;

import com.minhtriet.se3979.identityservice.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    // Tìm token trong DB để đối chiếu khi user yêu cầu refresh
    Optional<RefreshToken> findByTokenHash(String tokenHash);
}