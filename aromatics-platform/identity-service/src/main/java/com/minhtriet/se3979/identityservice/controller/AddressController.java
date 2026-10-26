package com.minhtriet.se3979.identityservice.controller;

import com.minhtriet.se3979.identityservice.dto.request.AddressRequest;
import com.minhtriet.se3979.identityservice.dto.response.AddressResponse;
import com.minhtriet.se3979.identityservice.dto.response.ApiResponse;
import com.minhtriet.se3979.identityservice.security.SecurityUtils;
import com.minhtriet.se3979.identityservice.service.AddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/identity/users/me/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AddressResponse>>> getMyAddresses() {
        Long userId = SecurityUtils.getCurrentUserId();
        List<AddressResponse> addresses = addressService.getUserAddresses(userId);
        return ResponseEntity.ok(ApiResponse.success(addresses, "Lấy danh sách địa chỉ thành công"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AddressResponse>> addAddress(@Valid @RequestBody AddressRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        AddressResponse created = addressService.addAddress(userId, request);
        return new ResponseEntity<>(ApiResponse.success(created, "Thêm địa chỉ mới thành công"), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AddressResponse>> updateAddress(
            @PathVariable Long id,
            @Valid @RequestBody AddressRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        AddressResponse updated = addressService.updateAddress(userId, id, request);
        return ResponseEntity.ok(ApiResponse.success(updated, "Cập nhật địa chỉ thành công"));
    }

    @PutMapping("/{id}/default")
    public ResponseEntity<ApiResponse<AddressResponse>> setDefaultAddress(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        AddressResponse updated = addressService.setDefaultAddress(userId, id);
        return ResponseEntity.ok(ApiResponse.success(updated, "Đặt làm địa chỉ mặc định thành công"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAddress(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        addressService.deleteAddress(userId, id);
        return ResponseEntity.ok(ApiResponse.success(null, "Xóa địa chỉ thành công"));
    }
}
