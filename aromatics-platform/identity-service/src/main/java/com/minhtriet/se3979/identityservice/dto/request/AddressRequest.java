package com.minhtriet.se3979.identityservice.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressRequest {

    @NotBlank(message = "Tên người nhận không được để trống")
    private String recipientName;

    @NotBlank(message = "Số điện thoại không được để trống")
    @JsonAlias({"recipientPhone", "phoneNumber", "phone"})
    private String recipientPhone;

    @Builder.Default
    @JsonAlias({"provinceCode", "provinceId"})
    private String provinceCode = "79";

    @NotBlank(message = "Tên tỉnh/thành không được để trống")
    @JsonAlias({"provinceName", "province", "city"})
    private String provinceName;

    @NotBlank(message = "Tên quận/huyện không được để trống")
    @JsonAlias({"districtName", "district"})
    private String districtName;

    @NotBlank(message = "Tên phường/xã không được để trống")
    @JsonAlias({"wardName", "ward"})
    private String wardName;

    @NotBlank(message = "Địa chỉ cụ thể không được để trống")
    @JsonAlias({"streetAddress", "address"})
    private String streetAddress;

    @Builder.Default
    private Boolean isDefault = false;
}
