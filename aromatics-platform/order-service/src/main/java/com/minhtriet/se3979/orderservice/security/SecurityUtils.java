package com.minhtriet.se3979.orderservice.security;

import com.minhtriet.se3979.orderservice.exception.AppException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public final class SecurityUtils {

    private SecurityUtils() {}

    public static Optional<AuthenticatedUser> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return Optional.empty();
        }
        Object principal = auth.getPrincipal();
        if (principal instanceof AuthenticatedUser u) {
            return Optional.of(u);
        }
        return Optional.empty();
    }

    public static AuthenticatedUser getRequiredCurrentUser() {
        return getCurrentUser().orElseThrow(() ->
                new AppException(HttpStatus.UNAUTHORIZED, "Chưa đăng nhập hoặc phiên làm việc đã hết hạn"));
    }

    public static Long getCurrentUserId() {
        return getRequiredCurrentUser().getUserId();
    }

    public static String getCurrentUserEmail() {
        return getRequiredCurrentUser().getEmail();
    }

    public static String getCurrentUserRole() {
        return getRequiredCurrentUser().getRole();
    }

    public static String getCurrentUserFullName() {
        return getRequiredCurrentUser().getFullName();
    }
}
