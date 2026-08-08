package com.shopsphere.shopsphere.util;

import com.shopsphere.shopsphere.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Small static helper so services don't each re-implement
 * "who is currently logged in?" pulling from Spring Security's context.
 * Works because our JWT filter (added in the Security step) puts a
 * fully-loaded User object as the Authentication's principal.
 */
public final class SecurityUtils {

    private SecurityUtils() {}

    public static User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user found in security context");
        }
        return (User) authentication.getPrincipal();
    }

    public static Long getCurrentUserId() {
        return getCurrentUser().getId();
    }
}
