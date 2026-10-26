package com.minhtriet.se3979.catalogservice.controller;

import com.minhtriet.se3979.catalogservice.dto.request.DeductRequest;
import com.minhtriet.se3979.catalogservice.dto.request.InventoryUpdateRequest;
import com.minhtriet.se3979.catalogservice.dto.request.RestoreStockRequest;
import com.minhtriet.se3979.catalogservice.dto.response.ApiResponse;
import com.minhtriet.se3979.catalogservice.dto.response.DeductResult;
import com.minhtriet.se3979.catalogservice.dto.response.InventoryResponse;
import com.minhtriet.se3979.catalogservice.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/catalog/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    // API: GET /api/catalog/inventory/variant/{variantId}
    @GetMapping("/variant/{variantId}")
    public ResponseEntity<ApiResponse<InventoryResponse>> getInventoryByVariantId(@PathVariable Long variantId) {
        InventoryResponse response = inventoryService.getInventoryByVariantId(variantId);
        return ResponseEntity.ok(ApiResponse.success(response, "Lấy thông tin tồn kho thành công"));
    }

    // API: PUT /api/catalog/inventory/variant/{variantId} (STAFF, ADMIN)
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    @PutMapping("/variant/{variantId}")
    public ResponseEntity<ApiResponse<InventoryResponse>> updateStock(
            @PathVariable Long variantId,
            @Valid @RequestBody InventoryUpdateRequest request) {
        InventoryResponse response = inventoryService.updateStock(variantId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Cập nhật tồn kho thành công"));
    }

    // API: GET /api/catalog/inventory/low-stock (STAFF, ADMIN)
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    @GetMapping("/low-stock")
    public ResponseEntity<ApiResponse<List<InventoryResponse>>> getLowStockInventories() {
        List<InventoryResponse> response = inventoryService.getLowStockInventories();
        return ResponseEntity.ok(ApiResponse.success(response, "Lấy danh sách hàng sắp hết thành công"));
    }

    // API: POST /api/catalog/inventory/deduct (Internal Feign / Checkout)
    @PostMapping("/deduct")
    public ResponseEntity<DeductResult> deductInventory(@Valid @RequestBody DeductRequest request) {
        DeductResult result = inventoryService.deductStock(request.getVariantId(), request.getQuantity());
        if (result.isSuccess()) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(result);
        }
    }

    // API: POST /api/catalog/inventory/restore (Internal Feign / Order Cancellation)
    @PostMapping("/restore")
    public ResponseEntity<ApiResponse<String>> restoreInventory(@Valid @RequestBody RestoreStockRequest request) {
        inventoryService.restoreStock(request.getVariantId(), request.getQuantity());
        return ResponseEntity.ok(ApiResponse.success("OK", "Hoàn trả tồn kho thành công"));
    }
}