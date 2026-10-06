package com.minhtriet.se3979.catalogservice.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ReviewResponse {
    private Long id;
    private Long userId;          // Để Frontend gọi sang User Service lấy tên/avatar khách hàng
    private String variantName;   // Hiển thị khách đã mua loại nào (VD: Hộp 500g)
    private Integer rating;
    private String title;
    private String content;
    private List<String> images;  // Danh sách link ảnh Cloudinary
    private String adminReply;    // Câu trả lời của Shop (nếu có)
    private Boolean isVisible;
    private LocalDateTime createdAt;
}