package com.minhtriet.se3979.identityservice.config;

import com.minhtriet.se3979.identityservice.entity.User;
import com.minhtriet.se3979.identityservice.enums.Provider;
import com.minhtriet.se3979.identityservice.enums.Role;
import com.minhtriet.se3979.identityservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            log.info("🌱 Đang khởi tạo dữ liệu mẫu 3 tài khoản cho Mộc Hương Shop...");

            User admin = User.builder()
                    .email("admin@mochuong.vn")
                    .passwordHash(passwordEncoder.encode("Admin@123"))
                    .fullName("Quản Trị Viên Mộc Hương")
                    .phoneNumber("0901234567")
                    .role(Role.ADMIN)
                    .provider(Provider.LOCAL)
                    .isActive(true)
                    .isEmailVerified(true)
                    .build();

            User staff = User.builder()
                    .email("staff@mochuong.vn")
                    .passwordHash(passwordEncoder.encode("Staff@123"))
                    .fullName("Nhân Viên Vận Hành")
                    .phoneNumber("0901234568")
                    .role(Role.STAFF)
                    .provider(Provider.LOCAL)
                    .isActive(true)
                    .isEmailVerified(true)
                    .build();

            User customer = User.builder()
                    .email("customer@mochuong.vn")
                    .passwordHash(passwordEncoder.encode("Customer@123"))
                    .fullName("Khách Hàng Mộc Hương")
                    .phoneNumber("0901234569")
                    .role(Role.CUSTOMER)
                    .provider(Provider.LOCAL)
                    .isActive(true)
                    .isEmailVerified(true)
                    .build();

            userRepository.save(admin);
            userRepository.save(staff);
            userRepository.save(customer);

            log.info("✅ Đã khởi tạo thành công 3 tài khoản mẫu: admin@mochuong.vn, staff@mochuong.vn, customer@mochuong.vn");
        }
    }
}
