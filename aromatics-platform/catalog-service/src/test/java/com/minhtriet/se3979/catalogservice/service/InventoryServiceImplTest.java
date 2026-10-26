package com.minhtriet.se3979.catalogservice.service;

import com.minhtriet.se3979.catalogservice.dto.response.DeductResult;
import com.minhtriet.se3979.catalogservice.dto.response.InventoryResponse;
import com.minhtriet.se3979.catalogservice.entity.Inventory;
import com.minhtriet.se3979.catalogservice.exception.AppException;
import com.minhtriet.se3979.catalogservice.repository.InventoryRepository;
import com.minhtriet.se3979.catalogservice.service.impl.InventoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.http.HttpStatus;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceImplTest {

    @Mock
    private RedissonClient redissonClient;

    @Mock
    private InventoryRepository inventoryRepo;

    @Mock
    private ProductCacheService productCacheService;

    @Mock
    private RLock rLock;

    @InjectMocks
    private InventoryServiceImpl inventoryService;

    private Inventory sampleInventory;

    @BeforeEach
    void setUp() {
        sampleInventory = Inventory.builder()
                .id(1L)
                .quantity(50)
                .reservedQuantity(0)
                .lowStockThreshold(10)
                .build();
    }

    @Test
    @DisplayName("Lấy thông tin tồn kho thành công")
    void testGetInventoryByVariantId_Success() {
        when(inventoryRepo.findByVariantId(1L)).thenReturn(Optional.of(sampleInventory));

        InventoryResponse response = inventoryService.getInventoryByVariantId(1L);

        assertNotNull(response);
        assertEquals(50, response.getQuantity());
        assertEquals(50, response.getAvailableQuantity());
        assertFalse(response.getIsLowStock());
    }

    @Test
    @DisplayName("Lấy thông tin tồn kho thất bại khi variant không tồn tại")
    void testGetInventoryByVariantId_NotFound_ThrowsAppException() {
        when(inventoryRepo.findByVariantId(999L)).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class, () -> inventoryService.getInventoryByVariantId(999L));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
    }

    @Test
    @DisplayName("Trừ tồn kho thành công khi số lượng đủ")
    void testDeductStock_Success() throws Exception {
        when(redissonClient.getLock(anyString())).thenReturn(rLock);
        when(rLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(true);
        when(rLock.isHeldByCurrentThread()).thenReturn(true);
        when(inventoryRepo.findByVariantIdWithLock(1L)).thenReturn(Optional.of(sampleInventory));

        DeductResult result = inventoryService.deductStock(1L, 10);

        assertTrue(result.isSuccess());
        assertEquals(40, sampleInventory.getQuantity());
        verify(inventoryRepo, times(1)).save(sampleInventory);
        verify(rLock, times(1)).unlock();
    }

    @Test
    @DisplayName("Trừ tồn kho thất bại khi số lượng tồn kho không đủ")
    void testDeductStock_InsufficientStock_ReturnsFail() throws Exception {
        when(redissonClient.getLock(anyString())).thenReturn(rLock);
        when(rLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(true);
        when(rLock.isHeldByCurrentThread()).thenReturn(true);
        when(inventoryRepo.findByVariantIdWithLock(1L)).thenReturn(Optional.of(sampleInventory));

        DeductResult result = inventoryService.deductStock(1L, 100);

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("không đủ số lượng"));
        verify(inventoryRepo, never()).save(any(Inventory.class));
        verify(rLock, times(1)).unlock();
    }

    @Test
    @DisplayName("Hoàn trả kho (compensate) thành công")
    void testRestoreStock_Success() throws Exception {
        when(redissonClient.getLock(anyString())).thenReturn(rLock);
        when(rLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(true);
        when(rLock.isHeldByCurrentThread()).thenReturn(true);
        when(inventoryRepo.findByVariantId(1L)).thenReturn(Optional.of(sampleInventory));

        inventoryService.restoreStock(1L, 20);

        assertEquals(70, sampleInventory.getQuantity());
        verify(inventoryRepo, times(1)).save(sampleInventory);
        verify(rLock, times(1)).unlock();
    }
}
