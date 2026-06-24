package com.minhtriet.se3979.orderservice.service;

import com.minhtriet.se3979.orderservice.dto.request.AddToCartRequest;
import com.minhtriet.se3979.orderservice.redis.CartItem;

import java.util.List;

public interface CartService {
    void addToCart(Long userId, AddToCartRequest request);
    List<CartItem> getCart(Long userId);
    void removeCartItem(Long userId, Long productId, Long variantId);
    void clearCart(Long userId);
}