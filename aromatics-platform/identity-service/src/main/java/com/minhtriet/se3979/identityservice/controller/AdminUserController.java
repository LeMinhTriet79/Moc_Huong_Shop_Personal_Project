package com.minhtriet.se3979.identityservice.controller;

import com.minhtriet.se3979.identityservice.dto.request.UpdateRoleRequest;
import com.minhtriet.se3979.identityservice.dto.request.UpdateStatusRequest;
import com.minhtriet.se3979.identityservice.dto.response.ApiResponse;
import com.minhtriet.se3979.identityservice.dto.response.UserResponse;
import com.minhtriet.se3979.identityservice.enums.Role;
import com.minhtriet.se3979.identityservice.security.SecurityUtils;
import com.minhtriet.se3979.identityservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/identity/admin/users")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;

    // 1. Phân trang & lọc danh sách người dùng
    @GetMapping
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getAllUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Page<UserResponse> users = userService.getAllUsers(keyword, role, isActive, PageRequest.of(page, size, sort));
        return ResponseEntity.ok(ApiResponse.success(users, "Lấy danh sách người dùng thành công"));
    }

    // 2. Xem chi tiết người dùng kèm danh sách địa chỉ
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserDetail(@PathVariable Long id) {
        UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success(user, "Lấy chi tiết người dùng thành công"));
    }

    // 3. Cập nhật quyền (Role) cho người dùng
    @PutMapping("/{id}/role")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserRole(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRoleRequest request) {
        Long currentAdminId = SecurityUtils.getCurrentUserId();
        UserResponse updated = userService.updateUserRole(currentAdminId, id, request.getRole());
        return ResponseEntity.ok(ApiResponse.success(updated, "Cập nhật quyền người dùng thành công"));
    }

    // 4. Khóa hoặc Mở khóa tài khoản người dùng
    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStatusRequest request) {
        UserResponse updated = userService.updateUserStatus(id, request.getIsActive());
        String msg = Boolean.TRUE.equals(request.getIsActive()) ? "Mở khóa tài khoản thành công" : "Đã khóa tài khoản người dùng và thu hồi mọi phiên đăng nhập";
        return ResponseEntity.ok(ApiResponse.success(updated, msg));
    }
}
