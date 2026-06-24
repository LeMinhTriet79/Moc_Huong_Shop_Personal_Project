package com.minhtriet.se3979.catalogservice.service.impl;

import com.minhtriet.se3979.catalogservice.dto.response.DeductResult;
import com.minhtriet.se3979.catalogservice.entity.Inventory;
import com.minhtriet.se3979.catalogservice.repository.InventoryRepository;
import com.minhtriet.se3979.catalogservice.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final RedissonClient redissonClient;
    private final InventoryRepository inventoryRepo;

    @Transactional
    public DeductResult deductStock(Long variantId, int quantity) {
        // 1. Tạo khóa (Lock) định danh duy nhất cho sản phẩm này
        String lockKey = "lock:inventory:variant:" + variantId;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            // 2. Thử lấy khóa: Đợi tối đa 3 giây nếu có người khác đang giữ khóa.
            // Nếu lấy được, khóa sẽ tự động nhả sau 10 giây (chống Deadlock).
            boolean isAcquired = lock.tryLock(3, 10, TimeUnit.SECONDS);

            if (!isAcquired) {
                log.warn("Không thể lấy lock cho variantId: {}", variantId);
                return DeductResult.fail("Hệ thống đang bận xử lý giao dịch khác, vui lòng thử lại!");
            }

            // === VÙNG CRITICAL SECTION: Chắc chắn chỉ 1 người chạy đoạn code này tại 1 thời điểm ===

            // Lấy tồn kho và kết hợp khóa PESSIMISTIC dưới database
            Inventory inv = inventoryRepo.findByVariantIdWithLock(variantId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin tồn kho"));

            // Kiểm tra số lượng
            if (inv.getQuantity() < quantity) {
                return DeductResult.fail("Sản phẩm không đủ số lượng trong kho. Còn lại: " + inv.getQuantity());
            }

            // Thực hiện trừ kho
            inv.setQuantity(inv.getQuantity() - quantity);
            inventoryRepo.save(inv);

            log.info("Trừ kho thành công {} sản phẩm cho variantId: {}", quantity, variantId);
            return DeductResult.success();

            // === KẾT THÚC CRITICAL SECTION ===

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Tiến trình bị gián đoạn khi đang chờ lock", e);
            return DeductResult.fail("Lỗi hệ thống khi xử lý tồn kho");
        } finally {
            // 3. LUÔN LUÔN nhả khóa (Unlock) ở block finally dù thành công hay lỗi
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}