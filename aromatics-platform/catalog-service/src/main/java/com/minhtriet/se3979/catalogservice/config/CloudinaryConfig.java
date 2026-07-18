package com.minhtriet.se3979.catalogservice.config;

import com.cloudinary.Cloudinary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CloudinaryConfig {

    // Lấy cái chuỗi URL từ file application.yml ra
    @Value("${cloudinary.url}")
    private String cloudinaryUrl;

    // Đánh dấu @Bean để Spring Boot biết đường mà khởi tạo thằng này
    @Bean
    public Cloudinary cloudinary() {
        Cloudinary cloudinary = new Cloudinary(cloudinaryUrl);
        // Ép buộc dùng HTTPS cho an toàn
        cloudinary.config.secure = true;
        return cloudinary;
    }
}