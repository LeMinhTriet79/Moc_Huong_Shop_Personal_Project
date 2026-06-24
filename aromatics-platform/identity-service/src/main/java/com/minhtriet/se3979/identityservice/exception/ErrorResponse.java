package com.minhtriet.se3979.identityservice.exception;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
public class ErrorResponse {
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;

    // Dùng để chứa danh sách các lỗi validation (VD: email sai định dạng, thiếu password)
    private Map<String, String> validationErrors;
}