package com.minhtriet.se3979.catalogservice.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
public class InternalAuthFilter extends OncePerRequestFilter {

    @Value("${app.internal-secret:MocHuongSecretKey2026InternalAuth}")
    private String internalSecret;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        String internalHeader = request.getHeader("X-Internal-Secret");

        // Nếu có X-Internal-Secret hợp lệ thì cấp quyền SYSTEM nội bộ
        if (internalSecret.equals(internalHeader)) {
            AuthenticatedUser systemUser = AuthenticatedUser.builder()
                    .userId(0L)
                    .email("system@mochuong.internal")
                    .role("SYSTEM")
                    .fullName("Mộc Hương System Internal")
                    .build();

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    systemUser, null, List.of(new SimpleGrantedAuthority("ROLE_SYSTEM"), new SimpleGrantedAuthority("ROLE_ADMIN")));

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }
}
