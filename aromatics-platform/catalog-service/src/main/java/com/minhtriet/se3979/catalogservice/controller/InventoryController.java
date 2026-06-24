package com.minhtriet.se3979.catalogservice.controller;

import com.minhtriet.se3979.catalogservice.dto.request.DeductRequest;
import com.minhtriet.se3979.catalogservice.dto.response.DeductResult;
import com.minhtriet.se3979.catalogservice.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/catalog/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    // API: POST /api/catalog/inventory/deduct
    @PostMapping("/deduct")
    public ResponseEntity<DeductResult> deductInventory(@Valid @RequestBody DeductRequest request) {
        DeductResult result = inventoryService.deductStock(request.getVariantId(), request.getQuantity());

        if (result.isSuccess()) {
            return ResponseEntity.ok(result);
        } else {
            // Trả về 409 Conflict nếu hết hàng hoặc lỗi lock
            return ResponseEntity.status(HttpStatus.CONFLICT).body(result);
        }
    }
}