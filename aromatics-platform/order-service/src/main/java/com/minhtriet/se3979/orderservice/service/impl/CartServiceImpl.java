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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final CatalogFeignClient catalogFeignClient;

    private static final String CART_KEY_PREFIX = "cart:user:";
    private final Map<Long, Map<String, CartItem>> fallbackCartStore = new ConcurrentHashMap<>();

    @Override
    public void addToCart(Long userId, AddToCartRequest req) {
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

        // Luôn đồng bộ vào In-Memory Fallback để sẵn sàng khi Redis offline
        Map<String, CartItem> userMemoryCart = fallbackCartStore.computeIfAbsent(userId, k -> new ConcurrentHashMap<>());
        CartItem memItem = userMemoryCart.get(field);
        if (memItem != null) {
            memItem.setQuantity(memItem.getQuantity() + req.getQuantity());
            memItem.setPrice(realPrice);
        } else {
            memItem = CartItem.builder()
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
        userMemoryCart.put(field, memItem);

        try {
            CartItem item;
            Object existingItemStr = redisTemplate.opsForHash().get(key, field);

            if (existingItemStr != null) {
                item = objectMapper.readValue(existingItemStr.toString(), CartItem.class);
                item.setQuantity(item.getQuantity() + req.getQuantity());
                item.setPrice(realPrice);
            } else {
                item = memItem;
            }

            redisTemplate.opsForHash().put(key, field, objectMapper.writeValueAsString(item));
            redisTemplate.expire(key, 30, TimeUnit.DAYS);
        } catch (Exception e) {
            log.warn("⚠️ Redis không khả dụng khi thêm giỏ hàng, sử dụng In-Memory Cart Fallback cho userId={}: {}", userId, e.getMessage());
        }
    }

    @Override
    public void updateCartItem(Long userId, UpdateCartItemRequest req) {
        String key = CART_KEY_PREFIX + userId;
        String field = req.getProductId() + "_" + req.getVariantId();

        Map<String, CartItem> userMemoryCart = fallbackCartStore.computeIfAbsent(userId, k -> new ConcurrentHashMap<>());
        if (req.getQuantity() <= 0) {
            userMemoryCart.remove(field);
            try {
                redisTemplate.opsForHash().delete(key, field);
            } catch (Exception ignored) {
            }
            return;
        }

        try {
            Object existingItemStr = redisTemplate.opsForHash().get(key, field);
            if (existingItemStr == null && !userMemoryCart.containsKey(field)) {
                throw new AppException(HttpStatus.NOT_FOUND, "Món hàng không tồn tại trong giỏ hàng!");
            }

            CartItem item = existingItemStr != null
                    ? objectMapper.readValue(existingItemStr.toString(), CartItem.class)
                    : userMemoryCart.get(field);

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
            userMemoryCart.put(field, item);
            redisTemplate.opsForHash().put(key, field, objectMapper.writeValueAsString(item));
            redisTemplate.expire(key, 30, TimeUnit.DAYS);
        } catch (AppException ae) {
            throw ae;
        } catch (Exception e) {
            CartItem fallbackItem = userMemoryCart.get(field);
            if (fallbackItem != null) {
                fallbackItem.setQuantity(req.getQuantity());
            }
            log.warn("⚠️ Redis không khả dụng khi cập nhật giỏ hàng, đã cập nhật trên In-Memory Fallback: {}", e.getMessage());
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
            if (!cartItems.isEmpty()) {
                Map<String, CartItem> syncMap = new LinkedHashMap<>();
                for (CartItem ci : cartItems) {
                    syncMap.put(ci.getProductId() + "_" + ci.getVariantId(), ci);
                }
                fallbackCartStore.put(userId, new ConcurrentHashMap<>(syncMap));
                return cartItems;
            }
        } catch (Exception e) {
            log.warn("⚠️ Redis không khả dụng khi đọc giỏ hàng, lấy từ In-Memory Fallback: {}", e.getMessage());
        }

        Map<String, CartItem> memCart = fallbackCartStore.get(userId);
        return memCart != null ? new ArrayList<>(memCart.values()) : cartItems;
    }

    @Override
    public void removeCartItem(Long userId, Long productId, Long variantId) {
        String key = CART_KEY_PREFIX + userId;
        String field = productId + "_" + variantId;
        Map<String, CartItem> memCart = fallbackCartStore.get(userId);
        if (memCart != null) {
            memCart.remove(field);
        }
        try {
            redisTemplate.opsForHash().delete(key, field);
        } catch (Exception e) {
            log.warn("⚠️ Redis offline khi xóa món hàng khỏi giỏ: {}", e.getMessage());
        }
    }

    @Override
    public void clearCart(Long userId) {
        fallbackCartStore.remove(userId);
        try {
            redisTemplate.delete(CART_KEY_PREFIX + userId);
        } catch (Exception e) {
            log.warn("⚠️ Redis offline khi xóa toàn bộ giỏ hàng: {}", e.getMessage());
        }
    }
}
