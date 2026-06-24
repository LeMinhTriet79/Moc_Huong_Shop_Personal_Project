package com.minhtriet.se3979.catalogservice.redis;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductCacheService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper; // Dùng để biến Object thành chuỗi JSON

    private static final String FEATURED_PRODUCTS_KEY = "cache:featured_products";
    private static final Duration TTL = Duration.ofMinutes(5); // Cache sống 5 phút

    // Lấy danh sách sản phẩm từ Redis
    public Optional<List<Object>> getFeaturedProducts() { // Đang dùng Object tạm, sau này thay bằng ProductDTO
        try {
            String json = redisTemplate.opsForValue().get(FEATURED_PRODUCTS_KEY);
            if (json == null) {
                return Optional.empty(); // Cache miss (Không có trên RAM)
            }
            // Cache hit (Có trên RAM) -> Parse JSON thành List
            List<Object> products = objectMapper.readValue(json, new TypeReference<>() {});
            return Optional.of(products);
        } catch (Exception e) {
            log.error("Lỗi khi đọc cache Redis", e);
            return Optional.empty();
        }
    }

    // Đẩy danh sách sản phẩm lên Redis
    public void cacheFeaturedProducts(List<Object> products) {
        try {
            String json = objectMapper.writeValueAsString(products);
            redisTemplate.opsForValue().set(FEATURED_PRODUCTS_KEY, json, TTL);
        } catch (Exception e) {
            log.error("Lỗi khi ghi cache Redis", e);
        }
    }
}