package com.minhtriet.se3979.catalogservice.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minhtriet.se3979.catalogservice.dto.response.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
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
    private final ObjectMapper objectMapper;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.setCharacterEncoding("UTF-8");
                            ApiResponse<Object> apiResponse = ApiResponse.error(401, "Unauthorized", "Bạn chưa đăng nhập hoặc phiên làm việc đã hết hạn!");
                            response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.setCharacterEncoding("UTF-8");
                            ApiResponse<Object> apiResponse = ApiResponse.error(403, "Forbidden", "Bạn không có quyền truy cập tài nguyên này!");
                            response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
                        })
                )
                .authorizeHttpRequests(auth -> auth
                        // Mở cửa public cho xem danh mục, sản phẩm, tồn kho theo biến thể và đánh giá
                        .requestMatchers(HttpMethod.GET, "/api/catalog/products/**", "/api/catalog/categories/**", "/api/catalog/inventory/variant/**", "/api/catalog/reviews/**").permitAll()
                        // Mở cửa public cho Chatbot AI tư vấn phong thủy trầm hương
                        .requestMatchers(HttpMethod.POST, "/api/catalog/ai/chat").permitAll()
                        // API nội bộ: trừ kho, hoàn kho, trừ kho theo lô, lấy chi tiết biến thể (yêu cầu SYSTEM hoặc STAFF/ADMIN)
                        .requestMatchers("/api/catalog/inventory/deduct", "/api/catalog/inventory/deduct-batch", "/api/catalog/inventory/restore",
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
