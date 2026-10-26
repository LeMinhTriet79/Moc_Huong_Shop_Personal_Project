package com.minhtriet.se3979.identityservice.service;

import com.minhtriet.se3979.identityservice.dto.request.LoginRequest;
import com.minhtriet.se3979.identityservice.dto.request.RegisterRequest;
import com.minhtriet.se3979.identityservice.dto.response.AuthResponse;
import com.minhtriet.se3979.identityservice.dto.response.UserResponse;
import com.minhtriet.se3979.identityservice.entity.EmailVerification;
import com.minhtriet.se3979.identityservice.entity.RefreshToken;
import com.minhtriet.se3979.identityservice.entity.User;
import com.minhtriet.se3979.identityservice.enums.Provider;
import com.minhtriet.se3979.identityservice.enums.Role;
import com.minhtriet.se3979.identityservice.exception.AppException;
import com.minhtriet.se3979.identityservice.kafka.AuthNotificationPublisher;
import com.minhtriet.se3979.identityservice.repository.EmailVerificationRepository;
import com.minhtriet.se3979.identityservice.repository.UserRepository;
import com.minhtriet.se3979.identityservice.security.JwtUtil;
import com.minhtriet.se3979.identityservice.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailVerificationRepository emailVerificationRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private TokenService tokenService;

    @Mock
    private AuthNotificationPublisher authNotificationPublisher;

    @InjectMocks
    private AuthServiceImpl authService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = User.builder()
                .id(1L)
                .email("test@mochuong.vn")
                .passwordHash("hashed_password")
                .fullName("Trần Hương Mộc")
                .role(Role.CUSTOMER)
                .provider(Provider.LOCAL)
                .isActive(true)
                .isEmailVerified(true)
                .build();
    }

    @Test
    @DisplayName("Đăng ký tài khoản thành công khi email chưa tồn tại")
    void testRegister_Success() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@mochuong.vn");
        request.setPassword("Password@123");
        request.setFullName("Trần Hương Mộc");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("hashed_password");
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);
        when(emailVerificationRepository.save(any(EmailVerification.class))).thenReturn(new EmailVerification());

        UserResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals(sampleUser.getId(), response.getId());
        assertEquals("test@mochuong.vn", response.getEmail());
        assertEquals("Trần Hương Mộc", response.getFullName());
        verify(userRepository, times(1)).save(any(User.class));
        verify(emailVerificationRepository, times(1)).save(any(EmailVerification.class));
        verify(authNotificationPublisher, times(1)).publishAuthEvent(any());
    }

    @Test
    @DisplayName("Đăng ký tài khoản thất bại khi email đã tồn tại")
    void testRegister_EmailAlreadyExists_ThrowsConflict() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@mochuong.vn");
        request.setPassword("Password@123");
        request.setFullName("Trần Hương Mộc");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        AppException ex = assertThrows(AppException.class, () -> authService.register(request));
        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
        assertEquals("Email này đã được sử dụng", ex.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Đăng nhập thành công khi thông tin chính xác và tài khoản đang kích hoạt")
    void testLogin_Success() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@mochuong.vn");
        request.setPassword("Password@123");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(sampleUser));
        when(jwtUtil.generateToken(sampleUser)).thenReturn("sample_access_token");

        AuthResponse response = authService.login(request, "127.0.0.1", "Chrome");

        assertNotNull(response);
        assertEquals("sample_access_token", response.getAccessToken());
        assertNotNull(response.getRefreshToken());
        assertEquals("test@mochuong.vn", response.getUser().getEmail());
        verify(tokenService, times(1)).revokeAllUserTokens(sampleUser);
        verify(tokenService, times(1)).saveRefreshToken(eq(sampleUser), anyString(), eq("127.0.0.1"), eq("Chrome"));
    }

    @Test
    @DisplayName("Đăng nhập thất bại khi sai email hoặc mật khẩu")
    void testLogin_BadCredentials_ThrowsUnauthorized() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@mochuong.vn");
        request.setPassword("WrongPassword");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        AppException ex = assertThrows(AppException.class, () -> authService.login(request, "127.0.0.1", "Chrome"));
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatus());
        assertEquals("Email hoặc mật khẩu không chính xác", ex.getMessage());
    }

    @Test
    @DisplayName("Đăng nhập thất bại khi tài khoản bị khóa")
    void testLogin_AccountDisabled_ThrowsForbidden() {
        sampleUser.setIsActive(false);

        LoginRequest request = new LoginRequest();
        request.setEmail("test@mochuong.vn");
        request.setPassword("Password@123");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(sampleUser));

        AppException ex = assertThrows(AppException.class, () -> authService.login(request, "127.0.0.1", "Chrome"));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
        assertEquals("Tài khoản của bạn đã bị khóa", ex.getMessage());
    }

    @Test
    @DisplayName("Làm mới token thành công")
    void testRefreshToken_Success() {
        RefreshToken storedToken = RefreshToken.builder()
                .id(1L)
                .tokenHash("hash_of_refresh_token")
                .user(sampleUser)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();

        when(tokenService.verifyAndRotateRefreshToken("valid_refresh_token", "127.0.0.1", "Chrome"))
                .thenReturn(storedToken);
        when(jwtUtil.generateToken(sampleUser)).thenReturn("new_access_token");

        AuthResponse response = authService.refreshToken("valid_refresh_token", "127.0.0.1", "Chrome");

        assertNotNull(response);
        assertEquals("new_access_token", response.getAccessToken());
        assertNotNull(response.getRefreshToken());
        assertEquals(sampleUser.getEmail(), response.getUser().getEmail());
    }
}
