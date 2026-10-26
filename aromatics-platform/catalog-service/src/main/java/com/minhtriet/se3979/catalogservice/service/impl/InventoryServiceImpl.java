package com.minhtriet.se3979.catalogservice.service.impl;

import com.minhtriet.se3979.catalogservice.dto.request.InventoryUpdateRequest;
import com.minhtriet.se3979.catalogservice.dto.response.DeductResult;
import com.minhtriet.se3979.catalogservice.dto.response.InventoryResponse;
import com.minhtriet.se3979.catalogservice.entity.Inventory;
import com.minhtriet.se3979.catalogservice.exception.AppException;
import com.minhtriet.se3979.catalogservice.repository.InventoryRepository;
import com.minhtriet.se3979.catalogservice.service.InventoryService;
import com.minhtriet.se3979.catalogservice.service.ProductCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final RedissonClient redissonClient;
    private final InventoryRepository inventoryRepo;
    private final ProductCacheService productCacheService;

    @Override
    public InventoryResponse getInventoryByVariantId(Long variantId) {
        Inventory inv = inventoryRepo.findByVariantId(variantId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy thông tin tồn kho cho biến thể: " + variantId));
        return mapToResponse(inv);
    }

    @Transactional
    @Override
    public InventoryResponse updateStock(Long variantId, InventoryUpdateRequest request) {
        String lockKey = "lock:inventory:variant:" + variantId;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            boolean acquired = lock.tryLock(5, 10, TimeUnit.SECONDS);
            if (!acquired) {
                throw new AppException(HttpStatus.CONFLICT, "Hệ thống đang xử lý kho cho biến thể này, vui lòng thử lại sau!");
            }

            Inventory inv = inventoryRepo.findByVariantIdWithLock(variantId)
                    .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy thông tin tồn kho cho biến thể: " + variantId));

            if (request.getQuantity() != null) {
                inv.setQuantity(request.getQuantity());
            }
            if (request.getLowStockThreshold() != null) {
                inv.setLowStockThreshold(request.getLowStockThreshold());
            }

            Inventory saved = inventoryRepo.save(inv);

            // Xóa cache sản phẩm vì số lượng tồn kho đã thay đổi
            if (saved.getVariant() != null && saved.getVariant().getProduct() != null) {
                productCacheService.evictProduct(saved.getVariant().getProduct().getSlug());
            }

            log.info("Cập nhật kho thành công cho variantId: {}, quantity={}, threshold={}",
                    variantId, saved.getQuantity(), saved.getLowStockThreshold());

            return mapToResponse(saved);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Tiến trình cập nhật tồn kho bị gián đoạn");
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Override
    public List<InventoryResponse> getLowStockInventories() {
        return inventoryRepo.findLowStockInventories().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    @Override
    public DeductResult deductStock(Long variantId, int quantity) {
        String lockKey = "lock:inventory:variant:" + variantId;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            boolean isAcquired = lock.tryLock(3, 10, TimeUnit.SECONDS);
            if (!isAcquired) {
                log.warn("Không thể lấy lock cho variantId: {}", variantId);
                return DeductResult.fail("Hệ thống đang bận xử lý giao dịch khác, vui lòng thử lại!");
            }

            Inventory inv = inventoryRepo.findByVariantIdWithLock(variantId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin tồn kho"));

            int available = inv.getQuantity() - inv.getReservedQuantity();
            if (available < quantity) {
                return DeductResult.fail("Sản phẩm không đủ số lượng trong kho. Khả dụng: " + available);
            }

            inv.setQuantity(inv.getQuantity() - quantity);
            inventoryRepo.save(inv);

            if (inv.getQuantity() <= inv.getLowStockThreshold()) {
                log.warn("⚠️ CẢNH BÁO LOW-STOCK: Biến thể {} còn {} sản phẩm (ngưỡng: {})",
                        variantId, inv.getQuantity(), inv.getLowStockThreshold());
            }

            // Xóa cache sản phẩm
            if (inv.getVariant() != null && inv.getVariant().getProduct() != null) {
                productCacheService.evictProduct(inv.getVariant().getProduct().getSlug());
            }

            log.info("Trừ kho thành công {} sản phẩm cho variantId: {}", quantity, variantId);
            return DeductResult.success();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Tiến trình bị gián đoạn khi đang chờ lock", e);
            return DeductResult.fail("Lỗi hệ thống khi xử lý tồn kho");
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Transactional
    @Override
    public void restoreStock(Long variantId, int quantity) {
        String lockKey = "lock:inventory:variant:" + variantId;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            boolean acquired = lock.tryLock(3, 10, TimeUnit.SECONDS);
            if (!acquired) {
                throw new RuntimeException("Hệ thống bận, không thể khóa kho để hoàn trả");
            }

            Inventory inv = inventoryRepo.findByVariantId(variantId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin tồn kho"));

            inv.setQuantity(inv.getQuantity() + quantity);
            inventoryRepo.save(inv);

            if (inv.getVariant() != null && inv.getVariant().getProduct() != null) {
                productCacheService.evictProduct(inv.getVariant().getProduct().getSlug());
            }

            log.info("[SAGA COMPENSATE] Đã HOÀN TRẢ thành công {} sản phẩm cho variantId: {}", quantity, variantId);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Khóa kho bị gián đoạn", e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    private InventoryResponse mapToResponse(Inventory inv) {
        int available = Math.max(0, inv.getQuantity() - inv.getReservedQuantity());
        boolean isLow = inv.getQuantity() <= inv.getLowStockThreshold();

        String variantName = null;
        String sku = null;
        Long productId = null;
        String productName = null;

        if (inv.getVariant() != null) {
            variantName = inv.getVariant().getVariantName();
            sku = inv.getVariant().getSku();
            if (inv.getVariant().getProduct() != null) {
                productId = inv.getVariant().getProduct().getId();
                productName = inv.getVariant().getProduct().getName();
            }
        }

        return InventoryResponse.builder()
                .id(inv.getId())
                .variantId(inv.getVariant() != null ? inv.getVariant().getId() : null)
                .variantName(variantName)
                .sku(sku)
                .productId(productId)
                .productName(productName)
                .quantity(inv.getQuantity())
                .reservedQuantity(inv.getReservedQuantity())
                .availableQuantity(available)
                .lowStockThreshold(inv.getLowStockThreshold())
                .isLowStock(isLow)
                .updatedAt(inv.getUpdatedAt())
                .build();
    }
}