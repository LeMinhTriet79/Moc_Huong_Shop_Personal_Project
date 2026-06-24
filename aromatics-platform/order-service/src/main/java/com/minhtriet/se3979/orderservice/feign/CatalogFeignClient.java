package com.minhtriet.se3979.orderservice.feign;

import com.minhtriet.se3979.orderservice.dto.request.DeductRequest;
import com.minhtriet.se3979.orderservice.dto.response.DeductResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

// Khai báo tên service cần gọi giống y hệt tên đã đăng ký trên Eureka
@FeignClient(name = "catalog-service")
public interface CatalogFeignClient {

    // Gọi thẳng vào API trừ kho của Catalog Service
    @PostMapping("/api/catalog/inventory/deduct")
    ResponseEntity<DeductResult> deductInventory(@RequestBody DeductRequest request);
}