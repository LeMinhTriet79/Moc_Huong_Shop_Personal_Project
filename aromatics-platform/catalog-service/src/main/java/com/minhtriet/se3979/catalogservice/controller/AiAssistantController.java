package com.minhtriet.se3979.catalogservice.controller;

import com.minhtriet.se3979.catalogservice.dto.request.AiChatRequest;
import com.minhtriet.se3979.catalogservice.dto.request.AiGenerateDescriptionRequest;
import com.minhtriet.se3979.catalogservice.dto.request.AiSuggestReplyRequest;
import com.minhtriet.se3979.catalogservice.dto.response.AiChatResponse;
import com.minhtriet.se3979.catalogservice.dto.response.ApiResponse;
import com.minhtriet.se3979.catalogservice.service.AiAssistantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/catalog/ai")
@RequiredArgsConstructor
public class AiAssistantController {

    private final AiAssistantService aiAssistantService;

    // 1. Chatbot Tư Vấn Phong Thủy & Trầm Hương (Mở cửa cho khách hàng hỏi đáp)
    @PostMapping("/chat")
    public ResponseEntity<ApiResponse<AiChatResponse>> chat(@Valid @RequestBody AiChatRequest request) {
        AiChatResponse response = aiAssistantService.chat(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Trợ lý AI Hương Đạo phản hồi thành công"));
    }

    // 2. Co-Pilot AI Viết Mô Tả Sản Phẩm Phong Thủy (Dành cho STAFF / ADMIN)
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    @PostMapping("/generate-description")
    public ResponseEntity<ApiResponse<AiChatResponse>> generateDescription(
            @Valid @RequestBody AiGenerateDescriptionRequest request) {
        AiChatResponse response = aiAssistantService.generateProductDescription(request);
        return ResponseEntity.ok(ApiResponse.success(response, "AI đã soạn thảo bài mô tả thành công"));
    }

    // 3. Co-Pilot AI Gợi Ý Phản Hồi Đánh Giá Khách Hàng (Dành cho STAFF / ADMIN)
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    @PostMapping("/suggest-reply")
    public ResponseEntity<ApiResponse<AiChatResponse>> suggestReply(
            @Valid @RequestBody AiSuggestReplyRequest request) {
        AiChatResponse response = aiAssistantService.suggestReviewReply(request);
        return ResponseEntity.ok(ApiResponse.success(response, "AI đã gợi ý câu phản hồi lịch thiệp"));
    }
}
