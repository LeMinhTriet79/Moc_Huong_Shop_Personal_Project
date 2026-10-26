package com.minhtriet.se3979.identityservice.service;

import com.minhtriet.se3979.identityservice.dto.response.WishlistResponse;

import java.util.List;

public interface WishlistService {
    List<WishlistResponse> getUserWishlist(Long userId);

    WishlistResponse addToWishlist(Long userId, Long productId);

    void removeFromWishlist(Long userId, Long productId);

    boolean isWishlisted(Long userId, Long productId);
}
