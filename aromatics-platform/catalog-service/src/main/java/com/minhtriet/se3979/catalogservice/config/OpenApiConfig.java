package com.minhtriet.se3979.catalogservice.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Catalog Service API - Mộc Hương",
                version = "1.0",
                description = "Tài liệu API cho dịch vụ Quản lý Sản phẩm và Tồn kho"
        ),
        servers = {
                @Server(url = "http://localhost:8080", description = "API Gateway")
        },
        // Áp dụng bảo mật JWT chung cho API (Mặc dù GET products ta đã mở public)
        security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Nhập Access Token (nếu API yêu cầu)"
)
public class OpenApiConfig {
}