package com.minhtriet.se3979.catalogservice.kafka;

import com.minhtriet.se3979.catalogservice.service.impl.InventoryServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderEventListener {

    private final InventoryServiceImpl inventoryService;

    @KafkaListener(topics = "order.cancelled", groupId = "catalog-group")
    public void handleOrderCancelled(Map<String, Object> orderData) {
        try {
            String orderCode = (String) orderData.get("orderCode");
            log.info("==================================================");
            log.info("🚨 KAFKA ALERT: Đơn hàng {} vừa bị HỦY!", orderCode);
            log.info("📦 Chuẩn bị chạy Giao dịch bù trừ (Compensating Transaction)...");

            // Lấy danh sách các món hàng (OrderItems) bên trong đơn hàng
            List<Map<String, Object>> items = (List<Map<String, Object>>) orderData.get("items");

            if (items != null) {
                for (Map<String, Object> item : items) {
                    Number variantIdNum = (Number) item.get("variantId");
                    Number quantityNum = (Number) item.get("quantity");

                    if (variantIdNum != null && quantityNum != null) {
                        // Trả lại kho cho từng món
                        inventoryService.restoreStock(variantIdNum.longValue(), quantityNum.intValue());
                    }
                }
            }
            log.info("==================================================");
        } catch (Exception e) {
            log.error("Lỗi khi xử lý hoàn trả kho: {}", e.getMessage());
        }
    }
}