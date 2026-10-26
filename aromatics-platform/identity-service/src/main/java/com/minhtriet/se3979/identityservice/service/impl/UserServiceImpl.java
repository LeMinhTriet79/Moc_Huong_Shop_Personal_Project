package com.minhtriet.se3979.identityservice.service.impl;

import com.minhtriet.se3979.identityservice.dto.request.ChangePasswordRequest;
import com.minhtriet.se3979.identityservice.dto.request.UpdateProfileRequest;
import com.minhtriet.se3979.identityservice.dto.response.UserResponse;
import com.minhtriet.se3979.identityservice.entity.User;
import com.minhtriet.se3979.identityservice.enums.Role;
import com.minhtriet.se3979.identityservice.exception.AppException;
import com.minhtriet.se3979.identityservice.repository.UserRepository;
import com.minhtriet.se3979.identityservice.service.TokenService;
import com.minhtriet.se3979.identityservice.service.UserService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    @Override
    public UserResponse getCurrentUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy người dùng"));

        return UserResponse.fromEntity(user);
    }

    @Override
    @Transactional
    public UserResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy người dùng"));

        if (request.getPhoneNumber() != null && !request.getPhoneNumber().isBlank()) {
            if (userRepository.existsByPhoneNumberAndIdNot(request.getPhoneNumber(), userId)) {
                throw new AppException(HttpStatus.CONFLICT, "Số điện thoại này đã được sử dụng bởi tài khoản khác");
            }
            user.setPhoneNumber(request.getPhoneNumber());
        }

        user.setFullName(request.getFullName());
        user.setDateOfBirth(request.getDateOfBirth());
        if (request.getAvatarUrl() != null) {
            user.setAvatarUrl(request.getAvatarUrl());
        }

        User updated = userRepository.save(user);
        return UserResponse.fromEntity(updated);
    }

    @Override
    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy người dùng"));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Mật khẩu cũ không chính xác");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Thu hồi toàn bộ Refresh Token cũ
        tokenService.revokeAllUserTokens(user);
    }

    @Override
    public UserResponse getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy người dùng"));
        return UserResponse.fromEntity(user);
    }

    @Override
    public Page<UserResponse> getAllUsers(String keyword, Role role, Boolean isActive, Pageable pageable) {
        Specification<User> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (keyword != null && !keyword.isBlank()) {
                String pattern = "%" + keyword.trim().toLowerCase() + "%";
                Predicate emailPredicate = cb.like(cb.lower(root.get("email")), pattern);
                Predicate namePredicate = cb.like(cb.lower(root.get("fullName")), pattern);
                Predicate phonePredicate = cb.like(cb.lower(root.get("phoneNumber")), pattern);
                predicates.add(cb.or(emailPredicate, namePredicate, phonePredicate));
            }

            if (role != null) {
                predicates.add(cb.equal(root.get("role"), role));
            }

            if (isActive != null) {
                predicates.add(cb.equal(root.get("isActive"), isActive));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return userRepository.findAll(spec, pageable).map(UserResponse::fromEntity);
    }

    @Override
    @Transactional
    public UserResponse updateUserRole(Long currentAdminId, Long targetUserId, Role newRole) {
        if (currentAdminId.equals(targetUserId)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Admin không thể tự thay đổi quyền hạn của chính mình");
        }

        User user = userRepository.findById(targetUserId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy người dùng mục tiêu"));

        user.setRole(newRole);
        User updated = userRepository.save(user);
        return UserResponse.fromEntity(updated);
    }

    @Override
    @Transactional
    public UserResponse updateUserStatus(Long targetUserId, Boolean isActive) {
        User user = userRepository.findById(targetUserId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy người dùng mục tiêu"));

        user.setIsActive(isActive);
        User updated = userRepository.save(user);

        if (Boolean.FALSE.equals(isActive)) {
            tokenService.revokeAllUserTokens(user);
        }

        return UserResponse.fromEntity(updated);
    }
}