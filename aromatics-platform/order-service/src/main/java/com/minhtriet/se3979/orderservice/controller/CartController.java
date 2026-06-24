package com.minhtriet.se3979.orderservice.controller;

import com.minhtriet.se3979.orderservice.dto.request.AddToCartRequest;
import com.minhtriet.se3979.orderservice.redis.CartItem;
import com.minhtriet.se3979.orderservice.security.JwtUtil;
import com.minhtriet.se3979.orderservice.service.CartService;
import jakarta.servlet.http.HttpServletRequest;
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
    private final JwtUtil jwtUtil;

    // Hàm tiện ích: Lấy ID người dùng từ Token JWT
    private Long getUserIdFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Không tìm thấy Token bảo mật");
        }
        String token = authHeader.substring(7);
        return jwtUtil.extractUserId(token);
    }

    // 1. Thêm vào giỏ hàng
    @PostMapping("/add")
    public ResponseEntity<String> addToCart(@Valid @RequestBody AddToCartRequest request, HttpServletRequest httpRequest) {
        Long userId = getUserIdFromRequest(httpRequest);
        cartService.addToCart(userId, request);
        return ResponseEntity.ok("Đã thêm vào giỏ hàng thành công");
    }

    // 2. Xem giỏ hàng
    @GetMapping
    public ResponseEntity<List<CartItem>> getCart(HttpServletRequest httpRequest) {
        Long userId = getUserIdFromRequest(httpRequest);
        return ResponseEntity.ok(cartService.getCart(userId));
    }

    // 3. Xóa 1 món đồ
    @DeleteMapping("/remove/{productId}/{variantId}")
    public ResponseEntity<String> removeCartItem(
            @PathVariable Long productId,
            @PathVariable Long variantId,
            HttpServletRequest httpRequest) {
        Long userId = getUserIdFromRequest(httpRequest);
        cartService.removeCartItem(userId, productId, variantId);
        return ResponseEntity.ok("Đã xóa sản phẩm khỏi giỏ hàng");
    }

    // 4. Xóa sạch giỏ hàng (Sau khi đặt hàng xong)
    @DeleteMapping("/clear")
    public ResponseEntity<String> clearCart(HttpServletRequest httpRequest) {
        Long userId = getUserIdFromRequest(httpRequest);
        cartService.clearCart(userId);
        return ResponseEntity.ok("Đã xóa sạch giỏ hàng");
    }
}