package com.minhtriet.se3979.identityservice.dto.response;

import com.minhtriet.se3979.identityservice.entity.Wishlist;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WishlistResponse {
    private Long id;
    private Long productId;
    private LocalDateTime createdAt;

    public static WishlistResponse fromEntity(Wishlist wishlist) {
        if (wishlist == null) return null;
        return WishlistResponse.builder()
                .id(wishlist.getId())
                .productId(wishlist.getProductId())
                .createdAt(wishlist.getCreatedAt())
                .build();
    }
}
