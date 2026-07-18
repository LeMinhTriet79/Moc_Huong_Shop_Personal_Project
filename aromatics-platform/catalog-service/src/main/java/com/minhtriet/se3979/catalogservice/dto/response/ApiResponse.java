package com.minhtriet.se3979.catalogservice.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL) // Bí quyết: Nếu data là null thì tự động giấu đi, JSON sẽ rất sạch
public class ApiResponse<T> {
    private int status;
    private String message;
    private T data;
    private String error;
    private LocalDateTime timestamp;

    // Hàm tiện ích trả về Thành công (Có kèm Data)
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .status(200)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    // Hàm tiện ích trả về Lỗi
    public static <T> ApiResponse<T> error(int status, String error, String message) {
        return ApiResponse.<T>builder()
                .status(status)
                .error(error)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }
}