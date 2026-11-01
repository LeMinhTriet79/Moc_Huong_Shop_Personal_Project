package com.minhtriet.se3979.identityservice;

import com.minhtriet.se3979.identityservice.dto.request.LoginRequest;
import com.minhtriet.se3979.identityservice.dto.request.RegisterRequest;
import com.minhtriet.se3979.identityservice.dto.response.AuthResponse;
import com.minhtriet.se3979.identityservice.dto.response.UserResponse;
import com.minhtriet.se3979.identityservice.exception.AppException;
import com.minhtriet.se3979.identityservice.security.JwtUtil;
import com.minhtriet.se3979.identityservice.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class IdentityServiceApplicationTests {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtUtil jwtUtil;

    @Test
    @DisplayName("Context loads successfully")
    void contextLoads() {
    }

    @Test
    @DisplayName("Đăng nhập thành công với tài khoản Admin mặc định")
    void testLogin_AdminSuccess() {
        LoginRequest request = new LoginRequest();
        request.setEmail("admin@mochuong.vn");
        request.setPassword("Admin@123");

        AuthResponse response = authService.login(request, "127.0.0.1", "JUnit-Test");

        assertNotNull(response);
        assertNotNull(response.getAccessToken());
        assertNotNull(response.getRefreshToken());
        assertEquals("ADMIN", response.getRole());
        assertEquals("admin@mochuong.vn", response.getEmail());
        assertTrue(jwtUtil.isTokenValid(response.getAccessToken(), "admin@mochuong.vn"));
    }

    @Test
    @DisplayName("Đăng nhập thành công với tài khoản Customer mặc định")
    void testLogin_CustomerSuccess() {
        LoginRequest request = new LoginRequest();
        request.setEmail("customer@mochuong.vn");
        request.setPassword("Customer@123");

        AuthResponse response = authService.login(request, "127.0.0.1", "JUnit-Test");

        assertNotNull(response);
        assertNotNull(response.getAccessToken());
        assertEquals("CUSTOMER", response.getRole());
        assertEquals("customer@mochuong.vn", response.getEmail());
    }

    @Test
    @DisplayName("Đăng nhập thất bại khi sai mật khẩu")
    void testLogin_WrongPassword() {
        LoginRequest request = new LoginRequest();
        request.setEmail("admin@mochuong.vn");
        request.setPassword("SaiMatKhau@123");

        AppException ex = assertThrows(AppException.class, () ->
                authService.login(request, "127.0.0.1", "JUnit-Test")
        );
        assertEquals(401, ex.getStatus().value());
    }

    @Test
    @DisplayName("Đăng ký tài khoản mới và đăng nhập ngay lập tức thành công")
    void testRegisterAndLogin_FlowSuccess() {
        String randomEmail = "user." + UUID.randomUUID().toString().substring(0, 8) + "@mochuong.test";
        String password = "Password@123";

        // 1. Đăng ký
        RegisterRequest registerReq = new RegisterRequest();
        registerReq.setEmail(randomEmail);
        registerReq.setPassword(password);
        registerReq.setFullName("Khách Hàng Thử Nghiệm");

        UserResponse userResponse = authService.register(registerReq);
        assertNotNull(userResponse);
        assertEquals(randomEmail, userResponse.getEmail());
        assertEquals("CUSTOMER", userResponse.getRole());

        // 2. Đăng nhập ngay lập tức
        LoginRequest loginReq = new LoginRequest();
        loginReq.setEmail(randomEmail);
        loginReq.setPassword(password);

        AuthResponse authResponse = authService.login(loginReq, "127.0.0.1", "JUnit-Test");
        assertNotNull(authResponse);
        assertNotNull(authResponse.getAccessToken());
        assertEquals("CUSTOMER", authResponse.getRole());
        assertEquals(randomEmail, authResponse.getEmail());
    }
}
