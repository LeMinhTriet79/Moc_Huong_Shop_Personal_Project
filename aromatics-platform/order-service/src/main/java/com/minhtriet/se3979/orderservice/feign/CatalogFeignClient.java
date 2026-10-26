package com.minhtriet.se3979.orderservice.feign;

import com.minhtriet.se3979.orderservice.dto.request.DeductRequest;
import com.minhtriet.se3979.orderservice.dto.response.ApiResponse;
import com.minhtriet.se3979.orderservice.dto.response.DeductResult;
import com.minhtriet.se3979.orderservice.dto.response.VariantInternalInfoResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "catalog-service")
public interface CatalogFeignClient {

    @PostMapping("/api/catalog/inventory/deduct")
    ResponseEntity<DeductResult> deductInventory(@RequestBody DeductRequest request);

    @GetMapping("/api/catalog/products/variants/{variantId}/internal")
    ResponseEntity<ApiResponse<VariantInternalInfoResponse>> getVariantInternalInfo(@PathVariable("variantId") Long variantId);
}