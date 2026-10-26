package com.minhtriet.se3979.orderservice.controller;

import com.minhtriet.se3979.orderservice.dto.request.CouponPreviewRequest;
import com.minhtriet.se3979.orderservice.dto.response.ApiResponse;
import com.minhtriet.se3979.orderservice.dto.response.CouponCalculationResult;
import com.minhtriet.se3979.orderservice.dto.response.CouponResponse;
import com.minhtriet.se3979.orderservice.redis.CartItem;
import com.minhtriet.se3979.orderservice.security.SecurityUtils;
import com.minhtriet.se3979.orderservice.service.CartService;
import com.minhtriet.se3979.orderservice.service.CouponService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/order/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;
    private final CartService cartService;

    // Lấy danh sách mã giảm giá khả dụng
    @GetMapping("/available")
    public ResponseEntity<ApiResponse<List<CouponResponse>>> getAvailableCoupons() {
        List<CouponResponse> coupons = couponService.getAvailableCoupons();
        return ResponseEntity.ok(ApiResponse.success(coupons, "Lấy danh sách mã giảm giá thành công"));
    }

    // Xem trước số tiền được giảm theo giỏ hàng hiện tại
    @PostMapping("/preview")
    public ResponseEntity<ApiResponse<CouponCalculationResult>> previewCoupon(
            @Valid @RequestBody CouponPreviewRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        List<CartItem> cartItems = cartService.getCart(userId);

        BigDecimal subtotal = cartItems.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Mặc định phí ship giả định để tính mã FREE_SHIPPING (VD: 30,000 đ)
        BigDecimal estimatedShippingFee = BigDecimal.valueOf(30000);

        CouponCalculationResult result = couponService.validateAndCalculateDiscount(
                userId, request.getCouponCode(), subtotal, estimatedShippingFee, cartItems);

        return ResponseEntity.ok(ApiResponse.success(result, result.getMessage()));
    }
}
