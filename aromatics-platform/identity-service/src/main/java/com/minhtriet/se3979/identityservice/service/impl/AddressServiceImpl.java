package com.minhtriet.se3979.identityservice.service.impl;

import com.minhtriet.se3979.identityservice.dto.request.AddressRequest;
import com.minhtriet.se3979.identityservice.dto.response.AddressResponse;
import com.minhtriet.se3979.identityservice.entity.Address;
import com.minhtriet.se3979.identityservice.entity.User;
import com.minhtriet.se3979.identityservice.exception.AppException;
import com.minhtriet.se3979.identityservice.repository.AddressRepository;
import com.minhtriet.se3979.identityservice.repository.UserRepository;
import com.minhtriet.se3979.identityservice.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    @Override
    public List<AddressResponse> getUserAddresses(Long userId) {
        return addressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(userId)
                .stream()
                .map(AddressResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional
    public AddressResponse addAddress(Long userId, AddressRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy người dùng"));

        boolean isFirst = addressRepository.countByUserId(userId) == 0;
        boolean shouldBeDefault = isFirst || Boolean.TRUE.equals(request.getIsDefault());

        if (shouldBeDefault) {
            addressRepository.clearDefaultForUser(userId);
        }

        Address address = Address.builder()
                .user(user)
                .recipientName(request.getRecipientName())
                .recipientPhone(request.getRecipientPhone())
                .provinceCode(request.getProvinceCode())
                .provinceName(request.getProvinceName())
                .districtName(request.getDistrictName())
                .wardName(request.getWardName())
                .streetAddress(request.getStreetAddress())
                .isDefault(shouldBeDefault)
                .build();

        Address saved = addressRepository.save(address);
        return AddressResponse.fromEntity(saved);
    }

    @Override
    @Transactional
    public AddressResponse updateAddress(Long userId, Long addressId, AddressRequest request) {
        Address address = addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy địa chỉ hoặc bạn không có quyền sửa"));

        if (Boolean.TRUE.equals(request.getIsDefault()) && !Boolean.TRUE.equals(address.getIsDefault())) {
            addressRepository.clearDefaultForUser(userId);
            address.setIsDefault(true);
        }

        address.setRecipientName(request.getRecipientName());
        address.setRecipientPhone(request.getRecipientPhone());
        address.setProvinceCode(request.getProvinceCode());
        address.setProvinceName(request.getProvinceName());
        address.setDistrictName(request.getDistrictName());
        address.setWardName(request.getWardName());
        address.setStreetAddress(request.getStreetAddress());

        Address updated = addressRepository.save(address);
        return AddressResponse.fromEntity(updated);
    }

    @Override
    @Transactional
    public AddressResponse setDefaultAddress(Long userId, Long addressId) {
        Address address = addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy địa chỉ hoặc bạn không có quyền thao tác"));

        addressRepository.clearDefaultForUser(userId);
        address.setIsDefault(true);
        Address updated = addressRepository.save(address);
        return AddressResponse.fromEntity(updated);
    }

    @Override
    @Transactional
    public void deleteAddress(Long userId, Long addressId) {
        Address address = addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy địa chỉ hoặc bạn không có quyền xóa"));

        addressRepository.delete(address);
    }
}
