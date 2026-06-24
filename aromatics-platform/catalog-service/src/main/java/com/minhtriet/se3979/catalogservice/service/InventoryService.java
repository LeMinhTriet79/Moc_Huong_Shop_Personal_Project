package com.minhtriet.se3979.catalogservice.service;

import com.minhtriet.se3979.catalogservice.dto.response.DeductResult;
import org.springframework.transaction.annotation.Transactional;

public interface InventoryService {
    DeductResult deductStock(Long variantId, int quantity);

    @Transactional
    void restoreStock(Long variantId, int quantity);
}