package com.minhtriet.se3979.identityservice.repository;

import com.minhtriet.se3979.identityservice.entity.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Long> {
    // Lấy danh sách sản phẩm yêu thích của 1 user
    List<Wishlist> findByUserId(Long userId);

    // Kiểm tra xem 1 user đã thích 1 sản phẩm cụ thể chưa
    boolean existsByUserIdAndProductId(Long userId, Long productId);
}