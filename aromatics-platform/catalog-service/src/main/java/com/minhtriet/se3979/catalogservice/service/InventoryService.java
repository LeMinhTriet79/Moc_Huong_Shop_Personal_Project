package com.minhtriet.se3979.catalogservice.service;

import com.minhtriet.se3979.catalogservice.dto.response.DeductResult;

public interface InventoryService {
    DeductResult deductStock(Long variantId, int quantity);
}