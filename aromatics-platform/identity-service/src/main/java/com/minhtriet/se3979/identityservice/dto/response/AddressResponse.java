package com.minhtriet.se3979.identityservice.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.minhtriet.se3979.identityservice.entity.Address;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressResponse {
    private Long id;
    private String recipientName;
    private String recipientPhone;
    private String provinceCode;
    private String provinceName;
    private String districtName;
    private String wardName;
    private String streetAddress;
    private Boolean isDefault;
    private LocalDateTime createdAt;

    // Aliases cho Frontend tiện dụng song ngữ
    @JsonProperty("phoneNumber")
    public String getPhoneNumber() {
        return recipientPhone;
    }

    @JsonProperty("phone")
    public String getPhone() {
        return recipientPhone;
    }

    @JsonProperty("province")
    public String getProvince() {
        return provinceName;
    }

    @JsonProperty("district")
    public String getDistrict() {
        return districtName;
    }

    @JsonProperty("ward")
    public String getWard() {
        return wardName;
    }

    public static AddressResponse fromEntity(Address address) {
        if (address == null) return null;
        return AddressResponse.builder()
                .id(address.getId())
                .recipientName(address.getRecipientName())
                .recipientPhone(address.getRecipientPhone())
                .provinceCode(address.getProvinceCode() != null ? address.getProvinceCode() : "79")
                .provinceName(address.getProvinceName())
                .districtName(address.getDistrictName())
                .wardName(address.getWardName())
                .streetAddress(address.getStreetAddress())
                .isDefault(address.getIsDefault())
                .createdAt(address.getCreatedAt())
                .build();
    }
}
