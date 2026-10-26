package com.minhtriet.se3979.identityservice.service.impl;

import com.minhtriet.se3979.identityservice.dto.response.WishlistResponse;
import com.minhtriet.se3979.identityservice.entity.User;
import com.minhtriet.se3979.identityservice.entity.Wishlist;
import com.minhtriet.se3979.identityservice.exception.AppException;
import com.minhtriet.se3979.identityservice.repository.UserRepository;
import com.minhtriet.se3979.identityservice.repository.WishlistRepository;
import com.minhtriet.se3979.identityservice.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WishlistServiceImpl implements WishlistService {

    private final WishlistRepository wishlistRepository;
    private final UserRepository userRepository;

    @Override
    public List<WishlistResponse> getUserWishlist(Long userId) {
        return wishlistRepository.findByUserId(userId)
                .stream()
                .map(WishlistResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional
    public WishlistResponse addToWishlist(Long userId, Long productId) {
        if (wishlistRepository.existsByUserIdAndProductId(userId, productId)) {
            Wishlist existing = wishlistRepository.findByUserIdAndProductId(userId, productId)
                    .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy"));
            return WishlistResponse.fromEntity(existing);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy người dùng"));

        Wishlist wishlist = Wishlist.builder()
                .user(user)
                .productId(productId)
                .build();

        Wishlist saved = wishlistRepository.save(wishlist);
        return WishlistResponse.fromEntity(saved);
    }

    @Override
    @Transactional
    public void removeFromWishlist(Long userId, Long productId) {
        wishlistRepository.deleteByUserIdAndProductId(userId, productId);
    }

    @Override
    public boolean isWishlisted(Long userId, Long productId) {
        return wishlistRepository.existsByUserIdAndProductId(userId, productId);
    }
}
