package com.minhtriet.se3979.orderservice.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minhtriet.se3979.orderservice.dto.request.AddToCartRequest;
import com.minhtriet.se3979.orderservice.redis.CartItem;
import com.minhtriet.se3979.orderservice.service.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper; // Dùng để ép kiểu Object thành JSON lưu cho dễ

    private static final String CART_KEY_PREFIX = "cart:user:";

    @Override
    public void addToCart(Long userId, AddToCartRequest req) {
        String key = CART_KEY_PREFIX + userId;
        // Gom ID sản phẩm và biến thể làm trường duy nhất trong Hash
        String field = req.getProductId() + "_" + req.getVariantId();

        try {
            CartItem item;
            // Kểm tra xem trong giỏ đã có món này chưa
            Object existingItemStr = redisTemplate.opsForHash().get(key, field);

            if (existingItemStr != null) {
                // Nếu có rồi thì cộng dồn số lượng
                item = objectMapper.readValue(existingItemStr.toString(), CartItem.class);
                item.setQuantity(item.getQuantity() + req.getQuantity());
            } else {
                // Nếu chưa có thì tạo mới
                item = CartItem.builder()
                        .productId(req.getProductId())
                        .variantId(req.getVariantId())
                        .productName(req.getProductName())
                        .variantName(req.getVariantName())
                        .productImageUrl(req.getProductImageUrl())
                        .price(req.getPrice())
                        .quantity(req.getQuantity())
                        .build();
            }

            // Lưu đè lại vào Redis
            redisTemplate.opsForHash().put(key, field, objectMapper.writeValueAsString(item));

            // Đặt thời gian sống cho giỏ hàng là 30 ngày [cite: 180]
            redisTemplate.expire(key, 30, TimeUnit.DAYS);

        } catch (Exception e) {
            log.error("Lỗi khi thêm vào giỏ hàng Redis", e);
            throw new RuntimeException("Lỗi hệ thống giỏ hàng");
        }
    }

    @Override
    public List<CartItem> getCart(Long userId) {
        String key = CART_KEY_PREFIX + userId;
        List<CartItem> cartItems = new ArrayList<>();

        try {
            // Kéo toàn bộ sản phẩm trong Hash ra
            Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);
            for (Object value : entries.values()) {
                cartItems.add(objectMapper.readValue(value.toString(), CartItem.class));
            }
        } catch (Exception e) {
            log.error("Lỗi khi lấy giỏ hàng từ Redis", e);
        }
        return cartItems;
    }

    @Override
    public void removeCartItem(Long userId, Long productId, Long variantId) {
        String key = CART_KEY_PREFIX + userId;
        String field = productId + "_" + variantId;
        redisTemplate.opsForHash().delete(key, field);
    }

    @Override
    public void clearCart(Long userId) {
        redisTemplate.delete(CART_KEY_PREFIX + userId);
    }
}