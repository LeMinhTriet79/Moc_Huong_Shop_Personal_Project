package com.minhtriet.se3979.catalogservice.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final InternalAuthFilter internalAuthFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Mở cửa public cho xem danh mục và sản phẩm
                        .requestMatchers(HttpMethod.GET, "/api/catalog/products/**", "/api/catalog/categories/**").permitAll()
                        // API nội bộ: trừ kho, hoàn kho, lấy chi tiết biến thể (yêu cầu SYSTEM hoặc STAFF/ADMIN)
                        .requestMatchers("/api/catalog/inventory/deduct", "/api/catalog/inventory/restore",
                                "/api/catalog/products/variants/*/internal", "/api/catalog/products/variants/batch-info")
                        .hasAnyRole("SYSTEM", "STAFF", "ADMIN")
                        // Mở cửa cho Swagger UI & Error
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/error").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(internalAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}