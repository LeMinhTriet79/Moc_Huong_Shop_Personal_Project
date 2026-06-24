package com.minhtriet.se3979.identityservice.service.impl;

import com.minhtriet.se3979.identityservice.dto.request.LoginRequest;
import com.minhtriet.se3979.identityservice.dto.request.RegisterRequest;
import com.minhtriet.se3979.identityservice.dto.response.AuthResponse;
import com.minhtriet.se3979.identityservice.dto.response.UserResponse;
import com.minhtriet.se3979.identityservice.entity.User;
import com.minhtriet.se3979.identityservice.enums.Provider;
import com.minhtriet.se3979.identityservice.enums.Role;
import com.minhtriet.se3979.identityservice.exception.AppException;
import com.minhtriet.se3979.identityservice.repository.UserRepository;
import com.minhtriet.se3979.identityservice.security.JwtUtil;
import com.minhtriet.se3979.identityservice.service.AuthService;
import com.minhtriet.se3979.identityservice.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;

    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {
        // Kiểm tra xem email đã có ai dùng chưa
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(HttpStatus.CONFLICT, "Email này đã được sử dụng");
        }

        // Tạo Entity User mới
        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword())) // Băm mật khẩu! Tuyệt đối ko lưu thô!
                .fullName(request.getFullName())
                .role(Role.CUSTOMER) // Mặc định khách hàng
                .provider(Provider.LOCAL)
                .isActive(true)
                .isEmailVerified(true) // Tạm thời để false, sau này làm luồng gửi OTP kích hoạt
                .build();

        // Lưu vào DB
        User savedUser = userRepository.save(user);

        return UserResponse.builder()
                .id(savedUser.getId())
                .email(savedUser.getEmail())
                .fullName(savedUser.getFullName())
                .role(savedUser.getRole().name())
                .createdAt(savedUser.getCreatedAt())
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest request, String ipAddress, String deviceInfo) {
        // Giao việc xác thực mật khẩu cho Spring Security lo
        // Giao việc xác thực mật khẩu cho Spring Security lo
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (org.springframework.security.authentication.BadCredentialsException e) {
            // Lỗi 1: Sai mật khẩu hoặc sai email
            throw new AppException(HttpStatus.UNAUTHORIZED, "Email hoặc mật khẩu không chính xác");
        } catch (org.springframework.security.authentication.DisabledException e) {
            // Lỗi 2: Tài khoản bị khóa hoặc chưa xác thực
            throw new AppException(HttpStatus.FORBIDDEN, "Tài khoản của bạn chưa được xác thực email hoặc đã bị khóa");
        } catch (Exception e) {
            // Lỗi 3: Các lỗi hệ thống khác
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Lỗi xác thực hệ thống");
        }

        // Lấy thông tin user (Lúc này chắc chắn đúng thông tin rồi)
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy người dùng"));

        if (!user.getIsActive()) {
            throw new AppException(HttpStatus.FORBIDDEN, "Tài khoản của bạn đã bị khóa");
        }

        // 1. Tạo Access Token bằng JWT
        String accessToken = jwtUtil.generateToken(user);

        // 2. Tạo Refresh Token (Một chuỗi ngẫu nhiên, không phải JWT)
        String refreshToken = UUID.randomUUID().toString();

        // 3. Thu hồi các token cũ (nếu có) và lưu token mới
        tokenService.revokeAllUserTokens(user);
        tokenService.saveRefreshToken(user, refreshToken, ipAddress, deviceInfo);

        // 4. Lấy luôn thông tin User để Frontend đỡ mất công gọi API 2 lần
        UserResponse userResponse = UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .avatarUrl(user.getAvatarUrl())
                .build();

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(userResponse)
                .build();
    }
}