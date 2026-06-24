package com.minhtriet.se3979.identityservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddressRequest {
    @NotBlank(message = "Tên người nhận không được để trống")
    private String recipientName;

    @NotBlank(message = "Số điện thoại không được để trống")
    private String recipientPhone;

    @NotBlank(message = "Mã tỉnh/thành không được để trống")
    private String provinceCode;

    @NotBlank(message = "Tên tỉnh/thành không được để trống")
    private String provinceName;

    @NotBlank(message = "Tên quận/huyện không được để trống")
    private String districtName;

    @NotBlank(message = "Tên phường/xã không được để trống")
    private String wardName;

    @NotBlank(message = "Địa chỉ cụ thể không được để trống")
    private String streetAddress;

    private Boolean isDefault = false;
}