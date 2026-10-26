package com.minhtriet.se3979.orderservice.controller;

import com.minhtriet.se3979.orderservice.dto.request.AddToCartRequest;
import com.minhtriet.se3979.orderservice.dto.request.UpdateCartItemRequest;
import com.minhtriet.se3979.orderservice.dto.response.ApiResponse;
import com.minhtriet.se3979.orderservice.redis.CartItem;
import com.minhtriet.se3979.orderservice.security.SecurityUtils;
import com.minhtriet.se3979.orderservice.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/order/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    // 1. Thêm vào giỏ hàng
    @PostMapping("/add")
    public ResponseEntity<ApiResponse<Void>> addToCart(@Valid @RequestBody AddToCartRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        cartService.addToCart(userId, request);
        return ResponseEntity.ok(ApiResponse.success(null, "Đã thêm vào giỏ hàng thành công"));
    }

    // 2. Cập nhật số lượng món đồ trong giỏ
    @PutMapping("/update")
    public ResponseEntity<ApiResponse<Void>> updateCartItem(@Valid @RequestBody UpdateCartItemRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        cartService.updateCartItem(userId, request);
        return ResponseEntity.ok(ApiResponse.success(null, "Cập nhật giỏ hàng thành công"));
    }

    // 3. Xem giỏ hàng
    @GetMapping
    public ResponseEntity<ApiResponse<List<CartItem>>> getCart() {
        Long userId = SecurityUtils.getCurrentUserId();
        List<CartItem> items = cartService.getCart(userId);
        return ResponseEntity.ok(ApiResponse.success(items, "Lấy giỏ hàng thành công"));
    }

    // 4. Xóa 1 món đồ
    @DeleteMapping("/remove/{productId}/{variantId}")
    public ResponseEntity<ApiResponse<Void>> removeCartItem(
            @PathVariable Long productId,
            @PathVariable Long variantId) {
        Long userId = SecurityUtils.getCurrentUserId();
        cartService.removeCartItem(userId, productId, variantId);
        return ResponseEntity.ok(ApiResponse.success(null, "Đã xóa sản phẩm khỏi giỏ hàng"));
    }

    // 5. Xóa sạch giỏ hàng
    @DeleteMapping("/clear")
    public ResponseEntity<ApiResponse<Void>> clearCart() {
        Long userId = SecurityUtils.getCurrentUserId();
        cartService.clearCart(userId);
        return ResponseEntity.ok(ApiResponse.success(null, "Đã xóa sạch giỏ hàng"));
    }
}