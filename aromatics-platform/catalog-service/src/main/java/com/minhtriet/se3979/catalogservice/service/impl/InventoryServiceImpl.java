package com.minhtriet.se3979.catalogservice.service.impl;

import com.minhtriet.se3979.catalogservice.dto.request.DeductRequest;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final RedissonClient redissonClient;
    private final InventoryRepository inventoryRepo;
    private final ProductCacheService productCacheService;

    @Autowired(required = false)
    private PlatformTransactionManager transactionManager;

    private <T> T executeInTx(Supplier<T> action) {
        if (transactionManager != null) {
            return new TransactionTemplate(transactionManager).execute(status -> action.get());
        }
        return action.get();
    }

    private void executeInTxWithoutResult(Runnable action) {
        if (transactionManager != null) {
            new TransactionTemplate(transactionManager).executeWithoutResult(status -> action.run());
        } else {
            action.run();
        }
    }

    @Override
    public InventoryResponse getInventoryByVariantId(Long variantId) {
        Inventory inv = inventoryRepo.findByVariantId(variantId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND,
                        "Không tìm thấy thông tin tồn kho cho biến thể: " + variantId));
        return mapToResponse(inv);
    }

    @Override
    public InventoryResponse updateStock(Long variantId, InventoryUpdateRequest request) {
        String lockKey = "lock:inventory:variant:" + variantId;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            if (!lock.tryLock(5, 10, TimeUnit.SECONDS)) {
                throw new AppException(HttpStatus.CONFLICT,
                        "Hệ thống đang xử lý kho cho biến thể này, vui lòng thử lại sau!");
            }

            return executeInTx(() -> {
                Inventory inv = inventoryRepo.findByVariantIdWithLock(variantId)
                        .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND,
                                "Không tìm thấy thông tin tồn kho cho biến thể: " + variantId));

                if (request.getQuantity() != null) {
                    inv.setQuantity(request.getQuantity());
                }
                if (request.getLowStockThreshold() != null) {
                    inv.setLowStockThreshold(request.getLowStockThreshold());
                }

                Inventory saved = inventoryRepo.save(inv);
                evictCacheSafely(saved);

                log.info("Cập nhật kho thành công cho variantId: {}, quantity={}, threshold={}",
                        variantId, saved.getQuantity(), saved.getLowStockThreshold());

                return mapToResponse(saved);
            });

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

    @Override
    public DeductResult deductStock(Long variantId, int quantity) {
        String lockKey = "lock:inventory:variant:" + variantId;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            if (!lock.tryLock(3, 10, TimeUnit.SECONDS)) {
                log.warn("Không thể lấy lock cho variantId: {}", variantId);
                return DeductResult.fail("Hệ thống đang bận xử lý giao dịch khác, vui lòng thử lại!");
            }

            return executeInTx(() -> {
                Inventory inv = inventoryRepo.findByVariantIdWithLock(variantId)
                        .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy thông tin tồn kho"));

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

                evictCacheSafely(inv);
                log.info("Trừ kho thành công {} sản phẩm cho variantId: {}", quantity, variantId);
                return DeductResult.success();
            });

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Tiến trình bị gián đoạn khi đang chờ lock", e);
            return DeductResult.fail("Lỗi hệ thống khi xử lý tồn kho");
        } catch (Exception e) {
            log.error("Lỗi khi trừ kho cho variantId {}: {}", variantId, e.getMessage());
            return DeductResult.fail(e.getMessage());
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Override
    public DeductResult deductStockBatch(List<DeductRequest> items) {
        if (items == null || items.isEmpty()) {
            return DeductResult.fail("Danh sách trừ kho không được để trống");
        }

        // Sắp xếp theo variantId tăng dần để triệt tiêu Deadlock giữa các thread đồng thời
        List<DeductRequest> sortedItems = items.stream()
                .sorted(Comparator.comparing(DeductRequest::getVariantId))
                .toList();

        List<RLock> acquiredLocks = new ArrayList<>();
        try {
            for (DeductRequest item : sortedItems) {
                String lockKey = "lock:inventory:variant:" + item.getVariantId();
                RLock lock = redissonClient.getLock(lockKey);
                boolean acquired = lock.tryLock(5, 10, TimeUnit.SECONDS);
                if (!acquired) {
                    return DeductResult.fail("Hệ thống đang bận xử lý tồn kho, vui lòng thử lại sau!");
                }
                acquiredLocks.add(lock);
            }

            return executeInTx(() -> {
                List<Inventory> inventoriesToUpdate = new ArrayList<>();
                for (DeductRequest item : sortedItems) {
                    Inventory inv = inventoryRepo.findByVariantIdWithLock(item.getVariantId())
                            .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND,
                                    "Không tìm thấy thông tin tồn kho cho biến thể: " + item.getVariantId()));

                    int available = inv.getQuantity() - inv.getReservedQuantity();
                    if (available < item.getQuantity()) {
                        String prodName = (inv.getVariant() != null && inv.getVariant().getProduct() != null)
                                ? inv.getVariant().getProduct().getName() : ("biến thể " + item.getVariantId());
                        return DeductResult.fail("Sản phẩm '" + prodName + "' không đủ số lượng trong kho. Khả dụng: " + available);
                    }
                    inventoriesToUpdate.add(inv);
                }

                for (int i = 0; i < sortedItems.size(); i++) {
                    DeductRequest item = sortedItems.get(i);
                    Inventory inv = inventoriesToUpdate.get(i);
                    inv.setQuantity(inv.getQuantity() - item.getQuantity());
                    inventoryRepo.save(inv);
                    evictCacheSafely(inv);
                }

                log.info("Trừ kho thành công theo lô cho {} sản phẩm", sortedItems.size());
                return DeductResult.success();
            });

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return DeductResult.fail("Lỗi hệ thống khi xử lý tồn kho theo lô");
        } catch (Exception e) {
            log.error("Lỗi khi trừ kho theo lô: {}", e.getMessage());
            return DeductResult.fail(e.getMessage());
        } finally {
            for (int i = acquiredLocks.size() - 1; i >= 0; i--) {
                RLock lock = acquiredLocks.get(i);
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        }
    }

    @Override
    public void restoreStock(Long variantId, int quantity) {
        String lockKey = "lock:inventory:variant:" + variantId;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            boolean acquired = lock.tryLock(3, 10, TimeUnit.SECONDS);
            if (!acquired) {
                throw new RuntimeException("Hệ thống bận, không thể khóa kho để hoàn trả");
            }

            executeInTxWithoutResult(() -> {
                Inventory inv = inventoryRepo.findByVariantIdWithLock(variantId)
                        .or(() -> inventoryRepo.findByVariantId(variantId))
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin tồn kho"));

                inv.setQuantity(inv.getQuantity() + quantity);
                inventoryRepo.save(inv);
                evictCacheSafely(inv);
            });

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

    private void evictCacheSafely(Inventory inv) {
        if (inv != null && inv.getVariant() != null && inv.getVariant().getProduct() != null) {
            productCacheService.evictProduct(inv.getVariant().getProduct().getSlug());
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
                .availableStock(available)
                .lowStockThreshold(inv.getLowStockThreshold())
                .isLowStock(isLow)
                .updatedAt(inv.getUpdatedAt())
                .build();
    }
}
