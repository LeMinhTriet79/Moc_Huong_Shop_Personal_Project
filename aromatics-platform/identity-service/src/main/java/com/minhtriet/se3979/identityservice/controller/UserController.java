package com.minhtriet.se3979.identityservice.controller;

import com.minhtriet.se3979.identityservice.dto.response.UserResponse;
import com.minhtriet.se3979.identityservice.security.JwtUtil;
import com.minhtriet.se3979.identityservice.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/identity/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    // API Lấy thông tin cá nhân: GET /api/identity/users/me
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(HttpServletRequest request) {
        // Lấy chuỗi Token từ Header "Authorization"
        String authHeader = request.getHeader("Authorization");

        // Cắt bỏ 7 ký tự đầu tiên ("Bearer ") để lấy đúng Token nguyên chất
        String token = authHeader.substring(7);

        // Trích xuất ID của User từ bên trong Token
        Long userId = jwtUtil.extractUserId(token);

        // Gọi Service truy vấn DB và trả về
        return ResponseEntity.ok(userService.getCurrentUserProfile(userId));
    }
}