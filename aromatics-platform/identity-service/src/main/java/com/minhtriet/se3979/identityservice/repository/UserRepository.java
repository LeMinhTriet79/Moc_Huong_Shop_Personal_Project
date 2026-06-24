package com.minhtriet.se3979.identityservice.repository;

import com.minhtriet.se3979.identityservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Tìm user theo email (dùng cho đăng nhập)
    Optional<User> findByEmail(String email);

    // Kiểm tra xem email đã tồn tại chưa (dùng cho đăng ký)
    boolean existsByEmail(String email);
}