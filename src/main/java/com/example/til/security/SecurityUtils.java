package com.example.til.security;

import java.util.Optional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

public final class SecurityUtils {
    private SecurityUtils() {}

    public static Optional<String> getCurrentUserEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return Optional.empty();
        Object principal = auth.getPrincipal();
        if (principal instanceof UserDetails ud) {
            return Optional.ofNullable(ud.getUsername());
        }
        if (principal instanceof String s) {
            return Optional.of(s);
        }
        return Optional.empty();
    }
}
