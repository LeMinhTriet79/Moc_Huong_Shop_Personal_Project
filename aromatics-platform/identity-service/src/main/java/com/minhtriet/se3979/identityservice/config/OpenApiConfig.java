package com.minhtriet.se3979.identityservice.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server; // <-- NHỚ IMPORT CÁI NÀY
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Identity Service API - Mộc Hương",
                version = "1.0",
                description = "Tài liệu API cho dịch vụ quản lý danh tính (Identity Service)"
        ),
        // Ép Swagger gọi qua cổng 8080 của API Gateway
        servers = {
                @Server(url = "http://localhost:8080", description = "API Gateway")
        },
        security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Nhập Access Token vào đây"
)
public class OpenApiConfig {
}