package com.minhtriet.se3979.catalogservice.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

@Slf4j
@Component
public class InternalAuthFilter extends OncePerRequestFilter {

    @Value("${app.internal-secret:MocHuongSecretKey2026InternalAuth}")
    private String internalSecret;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        String internalHeader = request.getHeader("X-Internal-Secret");
        String path = request.getRequestURI();

        boolean isInternalPath = path.startsWith("/api/catalog/inventory/deduct")
                || path.startsWith("/api/catalog/inventory/restore")
                || (path.contains("/variants/") && path.endsWith("/internal"))
                || path.endsWith("/variants/batch-info");

        if (isInternalPath && internalHeader != null && MessageDigest.isEqual(
                internalSecret.getBytes(StandardCharsets.UTF_8),
                internalHeader.getBytes(StandardCharsets.UTF_8))) {

            AuthenticatedUser systemUser = AuthenticatedUser.builder()
                    .userId(0L)
                    .email("system@mochuong.internal")
                    .role("SYSTEM")
                    .fullName("Mộc Hương System Internal")
                    .build();

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    systemUser,
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_SYSTEM"))
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }
}
