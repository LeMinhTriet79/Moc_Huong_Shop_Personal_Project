package com.minhtriet.se3979.orderservice.service;

import com.minhtriet.se3979.orderservice.dto.request.CouponRequest;
import com.minhtriet.se3979.orderservice.dto.response.CouponCalculationResult;
import com.minhtriet.se3979.orderservice.dto.response.CouponResponse;
import com.minhtriet.se3979.orderservice.entity.CouponUsage;
import com.minhtriet.se3979.orderservice.entity.Order;
import com.minhtriet.se3979.orderservice.redis.CartItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface CouponService {

    List<CouponResponse> getAvailableCoupons();

    CouponCalculationResult validateAndCalculateDiscount(Long userId, String couponCode, BigDecimal subtotal, BigDecimal shippingFee, List<CartItem> items);

    CouponUsage recordCouponUsage(String couponCode, Long userId, Order order, BigDecimal discountAmount);

    void refundCouponUsage(Long orderId);

    CouponResponse createCoupon(CouponRequest request);

    CouponResponse updateCoupon(Long id, CouponRequest request);

    void deleteCoupon(Long id);

    Page<CouponResponse> getAllCoupons(Pageable pageable);

    CouponResponse getCouponById(Long id);
}
