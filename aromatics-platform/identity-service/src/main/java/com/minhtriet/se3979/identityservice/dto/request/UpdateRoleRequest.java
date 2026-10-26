package com.minhtriet.se3979.identityservice.dto.request;

import com.minhtriet.se3979.identityservice.enums.Role;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRoleRequest {
    @NotNull(message = "Role không được để trống")
    private Role role;
}
