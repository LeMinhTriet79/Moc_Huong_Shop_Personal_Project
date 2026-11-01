package com.minhtriet.se3979.catalogservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiChatRequest {

    @NotBlank(message = "Nội dung câu hỏi không được để trống")
    private String message;

    private List<ChatMessageItem> history;

    private String userRole; // CUSTOMER, STAFF, ADMIN

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatMessageItem {
        private String role; // "user" hoặc "model"
        private String content;
    }
}
