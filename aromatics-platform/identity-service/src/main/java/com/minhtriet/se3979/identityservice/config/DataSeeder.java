package com.minhtriet.se3979.identityservice.config;

import com.minhtriet.se3979.identityservice.entity.Address;
import com.minhtriet.se3979.identityservice.entity.User;
import com.minhtriet.se3979.identityservice.entity.Wishlist;
import com.minhtriet.se3979.identityservice.enums.Provider;
import com.minhtriet.se3979.identityservice.enums.Role;
import com.minhtriet.se3979.identityservice.repository.AddressRepository;
import com.minhtriet.se3979.identityservice.repository.UserRepository;
import com.minhtriet.se3979.identityservice.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final WishlistRepository wishlistRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("ℹ️ Dữ liệu Identity Service đã tồn tại, bỏ qua bước seed.");
            return;
        }

        log.info("🌱 Đang khởi tạo dữ liệu mẫu chuẩn cho Identity Service (Users, Addresses, Wishlists)...");

        // 1. Seed 6 tài khoản (ID = 1..6 khớp với CatalogDataSeeder & OrderDataSeeder)
        User admin = userRepository.save(User.builder()
                .email("admin@mochuong.vn")
                .passwordHash(passwordEncoder.encode("Admin@123"))
                .fullName("Quản Trị Viên Mộc Hương")
                .phoneNumber("0901234567")
                .dateOfBirth(LocalDate.of(1990, 5, 15))
                .avatarUrl("https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=300&q=80")
                .role(Role.ADMIN)
                .provider(Provider.LOCAL)
                .isActive(true)
                .isEmailVerified(true)
                .build());

        User staff = userRepository.save(User.builder()
                .email("staff@mochuong.vn")
                .passwordHash(passwordEncoder.encode("Staff@123"))
                .fullName("Nghệ Nhân Vận Hành Mộc Hương")
                .phoneNumber("0901234568")
                .dateOfBirth(LocalDate.of(1995, 8, 20))
                .avatarUrl("https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=300&q=80")
                .role(Role.STAFF)
                .provider(Provider.LOCAL)
                .isActive(true)
                .isEmailVerified(true)
                .build());

        User customer1 = userRepository.save(User.builder()
                .email("customer@mochuong.vn")
                .passwordHash(passwordEncoder.encode("Customer@123"))
                .fullName("Nguyễn Minh Triết")
                .phoneNumber("0901234569")
                .dateOfBirth(LocalDate.of(1998, 10, 26))
                .avatarUrl("https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=300&q=80")
                .role(Role.CUSTOMER)
                .provider(Provider.LOCAL)
                .isActive(true)
                .isEmailVerified(true)
                .build());

        User customer2 = userRepository.save(User.builder()
                .email("thanhha.zen@gmail.com")
                .passwordHash(passwordEncoder.encode("Customer@123"))
                .fullName("Trần Thanh Hà")
                .phoneNumber("0912345678")
                .dateOfBirth(LocalDate.of(1993, 3, 12))
                .avatarUrl("https://images.unsplash.com/photo-1544005313-94ddf0286df2?auto=format&fit=crop&w=300&q=80")
                .role(Role.CUSTOMER)
                .provider(Provider.LOCAL)
                .isActive(true)
                .isEmailVerified(true)
                .build());

        User customer3 = userRepository.save(User.builder()
                .email("hoangnam.tra@gmail.com")
                .passwordHash(passwordEncoder.encode("Customer@123"))
                .fullName("Lê Hoàng Nam")
                .phoneNumber("0987654321")
                .dateOfBirth(LocalDate.of(1988, 11, 5))
                .avatarUrl("https://images.unsplash.com/photo-1492562080023-ab3db95bfbce?auto=format&fit=crop&w=300&q=80")
                .role(Role.CUSTOMER)
                .provider(Provider.LOCAL)
                .isActive(true)
                .isEmailVerified(true)
                .build());

        User customer4 = userRepository.save(User.builder()
                .email("anhthu.agarwood@gmail.com")
                .passwordHash(passwordEncoder.encode("Customer@123"))
                .fullName("Phạm Anh Thư")
                .phoneNumber("0938112233")
                .dateOfBirth(LocalDate.of(1996, 7, 19))
                .role(Role.CUSTOMER)
                .provider(Provider.LOCAL)
                .isActive(true)
                .isEmailVerified(true)
                .build());

        // 2. Seed Sổ địa chỉ giao hàng (Addresses) để trang /checkout & /account/addresses có sẵn dữ liệu
        if (addressRepository.count() == 0) {
            addressRepository.saveAll(List.of(
                    Address.builder()
                            .user(customer1)
                            .recipientName("Nguyễn Minh Triết")
                            .recipientPhone("0901234569")
                            .provinceCode("79")
                            .provinceName("TP. Hồ Chí Minh")
                            .districtName("Quận 1")
                            .wardName("Phường Bến Nghé")
                            .streetAddress("Số 108 Đường Nguyễn Huệ, Tòa nhà Sen Vàng")
                            .isDefault(true)
                            .build(),
                    Address.builder()
                            .user(customer1)
                            .recipientName("Nguyễn Minh Triết (Văn phòng Trà Đạo)")
                            .recipientPhone("0901234569")
                            .provinceCode("01")
                            .provinceName("TP. Hà Nội")
                            .districtName("Quận Hoàn Kiếm")
                            .wardName("Phường Hàng Trống")
                            .streetAddress("Số 36 Phố Nhà Thờ")
                            .isDefault(false)
                            .build(),
                    Address.builder()
                            .user(customer2)
                            .recipientName("Trần Thanh Hà")
                            .recipientPhone("0912345678")
                            .provinceCode("48")
                            .provinceName("TP. Đà Nẵng")
                            .districtName("Quận Hải Châu")
                            .wardName("Phường Thạch Thang")
                            .streetAddress("Số 24 Đường Bạch Đằng")
                            .isDefault(true)
                            .build(),
                    Address.builder()
                            .user(customer3)
                            .recipientName("Lê Hoàng Nam")
                            .recipientPhone("0987654321")
                            .provinceCode("79")
                            .provinceName("TP. Hồ Chí Minh")
                            .districtName("Quận 3")
                            .wardName("Phường Võ Thị Sáu")
                            .streetAddress("Số 88 Đường Pasteur")
                            .isDefault(true)
                            .build(),
                    Address.builder()
                            .user(admin)
                            .recipientName("Quản Trị Viên Mộc Hương")
                            .recipientPhone("0901234567")
                            .provinceCode("79")
                            .provinceName("TP. Hồ Chí Minh")
                            .districtName("Quận 1")
                            .wardName("Phường Đa Kao")
                            .streetAddress("Số 12 Đường Nguyễn Đình Chiểu")
                            .isDefault(true)
                            .build()
            ));
        }

        // 3. Seed Danh sách yêu thích (Wishlists) cho customer@mochuong.vn (Product IDs: 1, 3, 5, 6)
        if (wishlistRepository.count() == 0) {
            wishlistRepository.saveAll(List.of(
                    Wishlist.builder().user(customer1).productId(1L).build(),
                    Wishlist.builder().user(customer1).productId(3L).build(),
                    Wishlist.builder().user(customer1).productId(5L).build(),
                    Wishlist.builder().user(customer1).productId(6L).build()
            ));
        }

        log.info("✅ Đã khởi tạo 6 Users, 5 Địa chỉ giao hàng và 4 Sản phẩm yêu thích cho Identity Service!");
    }
}
