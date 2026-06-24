package com.minhtriet.se3979.identityservice.repository;

import com.minhtriet.se3979.identityservice.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
    // Lấy danh sách địa chỉ của 1 user cụ thể
    List<Address> findByUserId(Long userId);
}