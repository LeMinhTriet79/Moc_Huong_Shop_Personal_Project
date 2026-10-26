package com.minhtriet.se3979.identityservice.controller;

import com.minhtriet.se3979.identityservice.dto.response.ApiResponse;
import com.minhtriet.se3979.identityservice.dto.response.WishlistResponse;
import com.minhtriet.se3979.identityservice.security.SecurityUtils;
import com.minhtriet.se3979.identityservice.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/identity/users/me/wishlists")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<WishlistResponse>>> getMyWishlist() {
        Long userId = SecurityUtils.getCurrentUserId();
        List<WishlistResponse> wishlist = wishlistService.getUserWishlist(userId);
        return ResponseEntity.ok(ApiResponse.success(wishlist, "Lấy danh sách yêu thích thành công"));
    }

    @PostMapping("/{productId}")
    public ResponseEntity<ApiResponse<WishlistResponse>> addToWishlist(@PathVariable Long productId) {
        Long userId = SecurityUtils.getCurrentUserId();
        WishlistResponse response = wishlistService.addToWishlist(userId, productId);
        return new ResponseEntity<>(ApiResponse.success(response, "Đã thêm sản phẩm vào danh sách yêu thích"), HttpStatus.CREATED);
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<ApiResponse<Void>> removeFromWishlist(@PathVariable Long productId) {
        Long userId = SecurityUtils.getCurrentUserId();
        wishlistService.removeFromWishlist(userId, productId);
        return ResponseEntity.ok(ApiResponse.success(null, "Đã xóa sản phẩm khỏi danh sách yêu thích"));
    }

    @GetMapping("/{productId}/check")
    public ResponseEntity<ApiResponse<Boolean>> checkWishlisted(@PathVariable Long productId) {
        Long userId = SecurityUtils.getCurrentUserId();
        boolean isWishlisted = wishlistService.isWishlisted(userId, productId);
        return ResponseEntity.ok(ApiResponse.success(isWishlisted, "Kiểm tra trạng thái yêu thích"));
    }
}
