package com.minhtriet.se3979.orderservice.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minhtriet.se3979.orderservice.dto.request.AddToCartRequest;
import com.minhtriet.se3979.orderservice.dto.request.UpdateCartItemRequest;
import com.minhtriet.se3979.orderservice.dto.response.ApiResponse;
import com.minhtriet.se3979.orderservice.dto.response.VariantInternalInfoResponse;
import com.minhtriet.se3979.orderservice.exception.AppException;
import com.minhtriet.se3979.orderservice.feign.CatalogFeignClient;
import com.minhtriet.se3979.orderservice.redis.CartItem;
import com.minhtriet.se3979.orderservice.service.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    private final ObjectMapper objectMapper;
    private final CatalogFeignClient catalogFeignClient;

    private static final String CART_KEY_PREFIX = "cart:user:";

    @Override
    public void addToCart(Long userId, AddToCartRequest req) {
        // 1. Kiểm tra dữ liệu thật từ catalog-service qua FeignClient (Fail Fast & Chống Hack giá)
        String realProductName = req.getProductName();
        String realVariantName = req.getVariantName();
        String realImageUrl = req.getProductImageUrl();
        String realSku = "SKU-" + req.getVariantId();
        var realPrice = req.getPrice();

        try {
            ResponseEntity<ApiResponse<VariantInternalInfoResponse>> feignRes =
                    catalogFeignClient.getVariantInternalInfo(req.getVariantId());

            if (feignRes.getBody() != null && feignRes.getBody().getData() != null) {
                VariantInternalInfoResponse variant = feignRes.getBody().getData();
                if (!Boolean.TRUE.equals(variant.getIsActive())) {
                    throw new AppException(HttpStatus.BAD_REQUEST, "Sản phẩm hoặc biến thể này hiện đã ngừng kinh doanh!");
                }
                if (variant.getAvailableStock() != null && variant.getAvailableStock() < req.getQuantity()) {
                    throw new AppException(HttpStatus.BAD_REQUEST,
                            "Số lượng trong kho không đủ (chỉ còn " + variant.getAvailableStock() + " sản phẩm)!");
                }
                realProductName = variant.getProductName();
                realVariantName = variant.getVariantName();
                realImageUrl = variant.getPrimaryImageUrl();
                realSku = variant.getSku();
                realPrice = variant.getPrice();
            }
        } catch (AppException ae) {
            throw ae;
        } catch (Exception e) {
            log.warn("Không thể xác thực variant từ catalog-service, fallback về request data: {}", e.getMessage());
        }

        String key = CART_KEY_PREFIX + userId;
        String field = req.getProductId() + "_" + req.getVariantId();

        try {
            CartItem item;
            Object existingItemStr = redisTemplate.opsForHash().get(key, field);

            if (existingItemStr != null) {
                item = objectMapper.readValue(existingItemStr.toString(), CartItem.class);
                item.setQuantity(item.getQuantity() + req.getQuantity());
                item.setPrice(realPrice); // Cập nhật lại giá mới nhất
            } else {
                item = CartItem.builder()
                        .productId(req.getProductId())
                        .variantId(req.getVariantId())
                        .productName(realProductName)
                        .variantName(realVariantName)
                        .productImageUrl(realImageUrl)
                        .sku(realSku)
                        .price(realPrice)
                        .quantity(req.getQuantity())
                        .build();
            }

            redisTemplate.opsForHash().put(key, field, objectMapper.writeValueAsString(item));
            redisTemplate.expire(key, 30, TimeUnit.DAYS);

        } catch (Exception e) {
            log.error("Lỗi khi thêm vào giỏ hàng Redis", e);
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Lỗi hệ thống giỏ hàng: " + e.getMessage());
        }
    }

    @Override
    public void updateCartItem(Long userId, UpdateCartItemRequest req) {
        String key = CART_KEY_PREFIX + userId;
        String field = req.getProductId() + "_" + req.getVariantId();

        if (req.getQuantity() <= 0) {
            redisTemplate.opsForHash().delete(key, field);
            return;
        }

        try {
            Object existingItemStr = redisTemplate.opsForHash().get(key, field);
            if (existingItemStr == null) {
                throw new AppException(HttpStatus.NOT_FOUND, "Món hàng không tồn tại trong giỏ hàng!");
            }

            CartItem item = objectMapper.readValue(existingItemStr.toString(), CartItem.class);

            // Kiểm tra tồn kho trước khi cập nhật
            try {
                var feignRes = catalogFeignClient.getVariantInternalInfo(req.getVariantId());
                if (feignRes.getBody() != null && feignRes.getBody().getData() != null) {
                    VariantInternalInfoResponse variant = feignRes.getBody().getData();
                    if (variant.getAvailableStock() != null && variant.getAvailableStock() < req.getQuantity()) {
                        throw new AppException(HttpStatus.BAD_REQUEST,
                                "Số lượng trong kho không đủ (chỉ còn " + variant.getAvailableStock() + " sản phẩm)!");
                    }
                    item.setPrice(variant.getPrice());
                }
            } catch (AppException ae) {
                throw ae;
            } catch (Exception e) {
                log.warn("Lỗi kiểm tra kho qua FeignClient: {}", e.getMessage());
            }

            item.setQuantity(req.getQuantity());
            redisTemplate.opsForHash().put(key, field, objectMapper.writeValueAsString(item));
            redisTemplate.expire(key, 30, TimeUnit.DAYS);

        } catch (AppException ae) {
            throw ae;
        } catch (Exception e) {
            log.error("Lỗi khi cập nhật giỏ hàng", e);
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Lỗi khi cập nhật giỏ hàng: " + e.getMessage());
        }
    }

    @Override
    public List<CartItem> getCart(Long userId) {
        String key = CART_KEY_PREFIX + userId;
        List<CartItem> cartItems = new ArrayList<>();

        try {
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