package com.minhtriet.se3979.catalogservice.kafka;

import com.minhtriet.se3979.catalogservice.entity.Product;
import com.minhtriet.se3979.catalogservice.entity.ProductVariant;
import com.minhtriet.se3979.catalogservice.repository.ProductRepository;
import com.minhtriet.se3979.catalogservice.repository.ProductVariantRepository;
import com.minhtriet.se3979.catalogservice.service.ProductCacheService;
import com.minhtriet.se3979.catalogservice.service.impl.InventoryServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderEventListener {

    private final InventoryServiceImpl inventoryService;
    private final ProductVariantRepository productVariantRepository;
    private final ProductRepository productRepository;
    private final ProductCacheService productCacheService;

    // Lắng nghe sự kiện Đơn hàng bị hủy -> Hoàn kho
    @KafkaListener(topics = "order.cancelled", groupId = "catalog-group")
    public void handleOrderCancelled(Map<String, Object> orderData) {
        try {
            String orderCode = (String) orderData.get("orderCode");
            log.info("==================================================");
            log.info("🚨 KAFKA ALERT: Đơn hàng {} vừa bị HỦY!", orderCode);
            log.info("📦 Chuẩn bị chạy Giao dịch bù trừ (Compensating Transaction)...");

            List<Map<String, Object>> items = (List<Map<String, Object>>) orderData.get("items");

            if (items != null) {
                for (Map<String, Object> item : items) {
                    Number variantIdNum = (Number) item.get("variantId");
                    Number quantityNum = (Number) item.get("quantity");

                    if (variantIdNum != null && quantityNum != null) {
                        inventoryService.restoreStock(variantIdNum.longValue(), quantityNum.intValue());
                    }
                }
            }
            log.info("==================================================");
        } catch (Exception e) {
            log.error("Lỗi khi xử lý hoàn trả kho: {}", e.getMessage());
        }
    }

    // Lắng nghe sự kiện Đơn hàng được tạo -> Tăng total_sold của Product
    @Transactional
    @KafkaListener(topics = "order.created", groupId = "catalog-group")
    public void handleOrderCreated(Map<String, Object> orderData) {
        try {
            String orderCode = (String) orderData.get("orderCode");
            log.info("🛒 KAFKA: Nhận sự kiện order.created cho mã đơn: {}", orderCode);

            List<Map<String, Object>> items = (List<Map<String, Object>>) orderData.get("items");
            if (items != null && !items.isEmpty()) {
                for (Map<String, Object> item : items) {
                    Number variantIdNum = (Number) item.get("variantId");
                    Number quantityNum = (Number) item.get("quantity");

                    if (variantIdNum != null && quantityNum != null) {
                        Long variantId = variantIdNum.longValue();
                        int quantity = quantityNum.intValue();

                        productVariantRepository.findById(variantId).ifPresent(variant -> {
                            Product product = variant.getProduct();
                            if (product != null) {
                                int currentSold = product.getTotalSold() != null ? product.getTotalSold() : 0;
                                product.setTotalSold(currentSold + quantity);
                                productRepository.save(product);
                                productCacheService.evictProduct(product.getSlug());
                                log.info("Đã tăng totalSold (+{}) cho sản phẩm '{}' (ID: {})",
                                        quantity, product.getName(), product.getId());
                            }
                        });
                    }
                }
            }
        } catch (Exception e) {
            log.error("Lỗi khi xử lý tăng total_sold từ order.created: {}", e.getMessage());
        }
    }
}