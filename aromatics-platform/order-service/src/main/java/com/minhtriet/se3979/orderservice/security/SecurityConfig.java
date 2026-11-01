package com.minhtriet.se3979.orderservice.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minhtriet.se3979.orderservice.dto.response.ApiResponse;
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
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
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
                            ApiResponse<Object> apiResponse = ApiResponse.error(401, "Bạn chưa đăng nhập hoặc phiên làm việc đã hết hạn!");
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
                        // Cho phép truy cập Swagger UI & VNPay Return callback
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/error", "/api/order/payment/vnpay-return").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/order/coupons/available", "/api/order/coupons/active").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/order/coupons/preview").permitAll()
                        // Tất cả các API của giỏ hàng và đơn hàng ĐỀU PHẢI có quyền hợp lệ
                        .requestMatchers("/api/order/**").authenticated()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
