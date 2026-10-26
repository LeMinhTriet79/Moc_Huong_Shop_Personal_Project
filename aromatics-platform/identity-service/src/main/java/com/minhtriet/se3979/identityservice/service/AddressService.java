package com.minhtriet.se3979.identityservice.service;

import com.minhtriet.se3979.identityservice.dto.request.AddressRequest;
import com.minhtriet.se3979.identityservice.dto.response.AddressResponse;

import java.util.List;

public interface AddressService {
    List<AddressResponse> getUserAddresses(Long userId);

    AddressResponse addAddress(Long userId, AddressRequest request);

    AddressResponse updateAddress(Long userId, Long addressId, AddressRequest request);

    AddressResponse setDefaultAddress(Long userId, Long addressId);

    void deleteAddress(Long userId, Long addressId);
}
