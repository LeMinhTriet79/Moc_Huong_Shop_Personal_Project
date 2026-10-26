package com.minhtriet.se3979.catalogservice.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minhtriet.se3979.catalogservice.dto.response.ProductDetailResponse;
import com.minhtriet.se3979.catalogservice.dto.response.ProductResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductCacheService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String FEATURED_PRODUCTS_KEY = "cache:featured_products";
    private static final String SLUG_CACHE_PREFIX = "cache:product:slug:";
    private static final Duration FEATURED_TTL = Duration.ofMinutes(5);
    private static final Duration SLUG_TTL = Duration.ofMinutes(10);

    // Lấy danh sách sản phẩm nổi bật từ Redis
    public Optional<List<ProductResponse>> getFeaturedProducts() {
        try {
            String json = redisTemplate.opsForValue().get(FEATURED_PRODUCTS_KEY);
            if (json == null || json.isBlank()) {
                return Optional.empty();
            }
            List<ProductResponse> products = objectMapper.readValue(json, new TypeReference<List<ProductResponse>>() {});
            return Optional.of(products);
        } catch (Exception e) {
            log.error("Lỗi khi đọc cache featured_products từ Redis: {}", e.getMessage());
            return Optional.empty();
        }
    }

    // Ghi cache danh sách sản phẩm nổi bật
    public void cacheFeaturedProducts(List<ProductResponse> products) {
        try {
            String json = objectMapper.writeValueAsString(products);
            redisTemplate.opsForValue().set(FEATURED_PRODUCTS_KEY, json, FEATURED_TTL);
        } catch (Exception e) {
            log.error("Lỗi khi ghi cache featured_products vào Redis: {}", e.getMessage());
        }
    }

    // Lấy chi tiết sản phẩm theo slug từ Redis
    public Optional<ProductDetailResponse> getProductDetailBySlug(String slug) {
        try {
            String key = SLUG_CACHE_PREFIX + slug;
            String json = redisTemplate.opsForValue().get(key);
            if (json == null || json.isBlank()) {
                return Optional.empty();
            }
            ProductDetailResponse detail = objectMapper.readValue(json, ProductDetailResponse.class);
            return Optional.of(detail);
        } catch (Exception e) {
            log.error("Lỗi khi đọc cache product detail theo slug: {}", e.getMessage());
            return Optional.empty();
        }
    }

    // Ghi cache chi tiết sản phẩm theo slug
    public void cacheProductDetail(String slug, ProductDetailResponse detail) {
        try {
            String key = SLUG_CACHE_PREFIX + slug;
            String json = objectMapper.writeValueAsString(detail);
            redisTemplate.opsForValue().set(key, json, SLUG_TTL);
        } catch (Exception e) {
            log.error("Lỗi khi ghi cache product detail theo slug: {}", e.getMessage());
        }
    }

    // Xóa cache khi có cập nhật sản phẩm
    public void evictProduct(String slug) {
        try {
            if (slug != null && !slug.isBlank()) {
                redisTemplate.delete(SLUG_CACHE_PREFIX + slug);
            }
            redisTemplate.delete(FEATURED_PRODUCTS_KEY);
            log.debug("Đã xóa cache sản phẩm: slug={}, featured", slug);
        } catch (Exception e) {
            log.error("Lỗi khi xóa cache sản phẩm: {}", e.getMessage());
        }
    }

    // Xóa toàn bộ cache sản phẩm
    public void evictAllProductCaches() {
        try {
            redisTemplate.delete(FEATURED_PRODUCTS_KEY);
            Set<String> keys = redisTemplate.keys(SLUG_CACHE_PREFIX + "*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
            log.info("Đã xóa sạch toàn bộ cache sản phẩm trên Redis");
        } catch (Exception e) {
            log.error("Lỗi khi dọn dẹp toàn bộ cache sản phẩm: {}", e.getMessage());
        }
    }
}