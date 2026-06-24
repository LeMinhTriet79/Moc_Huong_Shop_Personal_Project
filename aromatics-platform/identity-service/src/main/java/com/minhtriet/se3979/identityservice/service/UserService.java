package com.minhtriet.se3979.identityservice.service;

import com.minhtriet.se3979.identityservice.dto.response.UserResponse;

public interface UserService {
    // Lấy thông tin chi tiết của user hiện tại
    UserResponse getCurrentUserProfile(Long userId);
}