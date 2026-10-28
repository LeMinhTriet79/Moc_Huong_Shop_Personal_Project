package com.minhtriet.se3979.catalogservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponse {
    private Long id;
    private Long productId;
    private String productName;
    private Long userId;          // Để Frontend gọi sang User Service lấy tên/avatar khách hàng
    private String userName;      // Mặc định "Khách hàng #" + userId nếu không có tên
    private String variantName;   // Hiển thị khách đã mua loại nào (VD: Hộp 500g)
    private Integer rating;
    private String title;
    private String content;
    private String comment;
    private List<String> images;  // Danh sách link ảnh Cloudinary
    private String adminReply;    // Câu trả lời của Shop (nếu có)
    private String replyMessage;
    private LocalDateTime repliedAt;
    private Boolean isVisible;
    private Boolean isVerifiedPurchase;
    private LocalDateTime createdAt;
}