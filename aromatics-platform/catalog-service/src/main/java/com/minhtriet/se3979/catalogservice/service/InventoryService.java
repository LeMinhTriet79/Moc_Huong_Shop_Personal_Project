package com.minhtriet.se3979.catalogservice.service;

import com.minhtriet.se3979.catalogservice.dto.request.InventoryUpdateRequest;
import com.minhtriet.se3979.catalogservice.dto.response.DeductResult;
import com.minhtriet.se3979.catalogservice.dto.response.InventoryResponse;

import java.util.List;

public interface InventoryService {

    InventoryResponse getInventoryByVariantId(Long variantId);

    InventoryResponse updateStock(Long variantId, InventoryUpdateRequest request);

    List<InventoryResponse> getLowStockInventories();

    DeductResult deductStock(Long variantId, int quantity);

    void restoreStock(Long variantId, int quantity);
}