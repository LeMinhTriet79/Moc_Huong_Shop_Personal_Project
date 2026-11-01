package com.minhtriet.se3979.identityservice.service.impl;

import com.minhtriet.se3979.identityservice.dto.event.AuthNotificationEvent;
import com.minhtriet.se3979.identityservice.dto.request.GoogleAuthRequest;
import com.minhtriet.se3979.identityservice.dto.request.LoginRequest;
import com.minhtriet.se3979.identityservice.dto.request.RegisterRequest;
import com.minhtriet.se3979.identityservice.dto.response.AuthResponse;
import com.minhtriet.se3979.identityservice.dto.response.UserResponse;
import com.minhtriet.se3979.identityservice.entity.EmailVerification;
import com.minhtriet.se3979.identityservice.entity.RefreshToken;
import com.minhtriet.se3979.identityservice.entity.User;
import com.minhtriet.se3979.identityservice.enums.Provider;
import com.minhtriet.se3979.identityservice.enums.Role;
import com.minhtriet.se3979.identityservice.enums.VerificationType;
import com.minhtriet.se3979.identityservice.exception.AppException;
import com.minhtriet.se3979.identityservice.kafka.AuthNotificationPublisher;
import com.minhtriet.se3979.identityservice.repository.EmailVerificationRepository;
import com.minhtriet.se3979.identityservice.repository.UserRepository;
import com.minhtriet.se3979.identityservice.security.JwtUtil;
import com.minhtriet.se3979.identityservice.service.AuthService;
import com.minhtriet.se3979.identityservice.service.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final EmailVerificationRepository emailVerificationRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final AuthNotificationPublisher authNotificationPublisher;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(HttpStatus.CONFLICT, "Email này đã được sử dụng");
        }

        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .role(Role.CUSTOMER)
                .provider(Provider.LOCAL)
                .isActive(true)
                .isEmailVerified(true) // Cho phép active ngay để tiện nghiệm thu, đồng thời vẫn gửi verify email event
                .build();

        User savedUser = userRepository.save(user);

        // Tạo mã xác thực Email
        String verifyToken = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(24);
        EmailVerification verification = EmailVerification.builder()
                .user(savedUser)
                .token(verifyToken)
                .type(VerificationType.VERIFY_EMAIL)
                .expiresAt(expiresAt)
                .isUsed(false)
                .build();
        emailVerificationRepository.save(verification);

        // Bắn sự kiện sang Kafka
        authNotificationPublisher.publishAuthEvent(AuthNotificationEvent.builder()
                .userId(savedUser.getId())
                .email(savedUser.getEmail())
                .fullName(savedUser.getFullName())
                .type(VerificationType.VERIFY_EMAIL.name())
                .token(verifyToken)
                .expiresAt(expiresAt)
                .build());

        return UserResponse.builder()
                .id(savedUser.getId())
                .email(savedUser.getEmail())
                .fullName(savedUser.getFullName())
                .role(savedUser.getRole().name())
                .createdAt(savedUser.getCreatedAt())
                .build();
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request, String ipAddress, String deviceInfo) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (BadCredentialsException e) {
            throw new AppException(HttpStatus.UNAUTHORIZED, "Email hoặc mật khẩu không chính xác");
        } catch (DisabledException e) {
            throw new AppException(HttpStatus.FORBIDDEN, "Tài khoản của bạn chưa được xác thực email hoặc đã bị khóa");
        } catch (Exception e) {
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Lỗi xác thực hệ thống: " + e.getMessage());
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy người dùng"));

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new AppException(HttpStatus.FORBIDDEN, "Tài khoản của bạn đã bị khóa");
        }

        String accessToken = jwtUtil.generateToken(user);
        String refreshToken = UUID.randomUUID().toString();

        tokenService.revokeAllUserTokens(user);
        tokenService.saveRefreshToken(user, refreshToken, ipAddress, deviceInfo);

        UserResponse userResponse = UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .avatarUrl(user.getAvatarUrl())
                .build();

        return AuthResponse.of(accessToken, refreshToken, userResponse);
    }

    @Override
    @Transactional
    public AuthResponse refreshToken(String refreshToken, String ipAddress, String deviceInfo) {
        RefreshToken storedToken = tokenService.verifyAndRotateRefreshToken(refreshToken, ipAddress, deviceInfo);
        User user = storedToken.getUser();

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new AppException(HttpStatus.FORBIDDEN, "Tài khoản của bạn đã bị khóa");
        }

        String newAccessToken = jwtUtil.generateToken(user);
        String newRefreshToken = UUID.randomUUID().toString();
        tokenService.saveRefreshToken(user, newRefreshToken, ipAddress, deviceInfo);

        UserResponse userResponse = UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .avatarUrl(user.getAvatarUrl())
                .build();

        return AuthResponse.of(newAccessToken, newRefreshToken, userResponse);
    }

    @Override
    @Transactional
    public void logout(String refreshToken) {
        tokenService.revokeToken(refreshToken);
    }

    @Override
    @Transactional
    public void verifyEmail(String token) {
        EmailVerification verification = emailVerificationRepository.findByTokenAndTypeAndIsUsedFalse(token, VerificationType.VERIFY_EMAIL)
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Mã xác thực không hợp lệ hoặc đã được sử dụng"));

        if (verification.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Mã xác thực đã hết hạn. Vui lòng yêu cầu gửi lại!");
        }

        verification.setIsUsed(true);
        emailVerificationRepository.save(verification);

        User user = verification.getUser();
        user.setIsEmailVerified(true);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy tài khoản với email này"));

        String resetToken = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(15);

        EmailVerification verification = EmailVerification.builder()
                .user(user)
                .token(resetToken)
                .type(VerificationType.RESET_PASSWORD)
                .expiresAt(expiresAt)
                .isUsed(false)
                .build();
        emailVerificationRepository.save(verification);

        authNotificationPublisher.publishAuthEvent(AuthNotificationEvent.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .type(VerificationType.RESET_PASSWORD.name())
                .token(resetToken)
                .expiresAt(expiresAt)
                .build());
    }

    @Override
    @Transactional
    public void resetPassword(String token, String newPassword) {
        EmailVerification verification = emailVerificationRepository.findByTokenAndTypeAndIsUsedFalse(token, VerificationType.RESET_PASSWORD)
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Mã đặt lại mật khẩu không hợp lệ hoặc đã được sử dụng"));

        if (verification.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Mã đặt lại mật khẩu đã hết hạn");
        }

        verification.setIsUsed(true);
        emailVerificationRepository.save(verification);

        User user = verification.getUser();
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Thu hồi toàn bộ Refresh Token cũ để ép đăng nhập lại với mật khẩu mới
        tokenService.revokeAllUserTokens(user);
    }

    @Override
    @Transactional
    public AuthResponse googleLogin(GoogleAuthRequest request, String ipAddress, String deviceInfo) {
        String email = request.getEmail();
        String fullName = request.getFullName();
        String avatarUrl = request.getAvatarUrl();
        String providerId = request.getProviderId();

        // Nếu client gửi idToken, tiến hành xác thực tokenInfo với Google
        if (request.getIdToken() != null && !request.getIdToken().isBlank()) {
            try {
                String googleTokenInfoUrl = "https://oauth2.googleapis.com/tokeninfo?id_token=" + request.getIdToken();
                ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                        googleTokenInfoUrl,
                        HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<>() {}
                );

                Map<String, Object> body = response.getBody();
                if (body == null || !body.containsKey("email")) {
                    throw new AppException(HttpStatus.UNAUTHORIZED, "Xác thực Google ID Token không hợp lệ");
                }

                String verifiedEmail = (String) body.get("email");
                Object emailVerifiedObj = body.get("email_verified");
                boolean emailVerified = emailVerifiedObj instanceof Boolean
                        ? (Boolean) emailVerifiedObj
                        : Boolean.parseBoolean(String.valueOf(emailVerifiedObj));

                if (!emailVerified) {
                    throw new AppException(HttpStatus.UNAUTHORIZED, "Tài khoản Google chưa được xác thực email");
                }

                email = verifiedEmail;
                fullName = body.containsKey("name") && body.get("name") != null
                        ? (String) body.get("name")
                        : (fullName != null && !fullName.isBlank() ? fullName : verifiedEmail);
                avatarUrl = body.containsKey("picture") && body.get("picture") != null
                        ? (String) body.get("picture")
                        : avatarUrl;
                providerId = body.containsKey("sub") && body.get("sub") != null
                        ? (String) body.get("sub")
                        : providerId;

            } catch (AppException ae) {
                throw ae;
            } catch (Exception e) {
                log.error("Lỗi khi xác thực ID Token với Google: {}", e.getMessage());
                throw new AppException(HttpStatus.UNAUTHORIZED, "Xác thực Google ID Token thất bại: " + e.getMessage());
            }
        }

        if (email == null || email.isBlank()) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Email không được để trống khi đăng nhập Google");
        }

        final String finalEmail = email;
        final String finalFullName = (fullName != null && !fullName.isBlank()) ? fullName : email;
        final String finalAvatarUrl = avatarUrl;
        final String finalProviderId = providerId;

        User user = userRepository.findByEmail(finalEmail).orElseGet(() -> {
            User newUser = User.builder()
                    .email(finalEmail)
                    .fullName(finalFullName)
                    .avatarUrl(finalAvatarUrl)
                    .provider(Provider.GOOGLE)
                    .providerId(finalProviderId)
                    .passwordHash(passwordEncoder.encode(UUID.randomUUID().toString()))
                    .role(Role.CUSTOMER)
                    .isActive(true)
                    .isEmailVerified(true)
                    .build();
            return userRepository.save(newUser);
        });

        // Chặn tuyệt đối đăng nhập Google vào tài khoản ADMIN hoặc STAFF nếu chưa cấu hình providerId khớp
        if (user.getRole() == Role.ADMIN || user.getRole() == Role.STAFF) {
            if (user.getProvider() != Provider.GOOGLE || user.getProviderId() == null
                    || !user.getProviderId().equals(finalProviderId)) {
                throw new AppException(HttpStatus.FORBIDDEN,
                        "Tài khoản Quản trị/Nhân viên bắt buộc đăng nhập bằng mật khẩu nội bộ!");
            }
        }

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new AppException(HttpStatus.FORBIDDEN, "Tài khoản đã bị khóa");
        }

        String accessToken = jwtUtil.generateToken(user);
        String refreshToken = UUID.randomUUID().toString();

        tokenService.revokeAllUserTokens(user);
        tokenService.saveRefreshToken(user, refreshToken, ipAddress, deviceInfo);

        UserResponse userResponse = UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .avatarUrl(user.getAvatarUrl())
                .build();

        return AuthResponse.of(accessToken, refreshToken, userResponse);
    }
}
