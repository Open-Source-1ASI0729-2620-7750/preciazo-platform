package com.preciazo.platform.shared.interfaces.rest.security;

import com.preciazo.platform.shared.application.result.ApplicationError;
import com.preciazo.platform.shared.application.result.ApplicationErrorException;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import java.security.Principal;
import java.util.Set;
import java.util.stream.Collectors;

/** Extracts authenticated user identity and roles from the server-verified Principal. */
public final class AuthenticatedPrincipal {
    private AuthenticatedPrincipal() { }

    public static long requireUserId(@Nullable Principal principal) {
        if (principal != null && principal.getName() != null && principal.getName().matches("[1-9][0-9]*")) {
            try {
                return Long.parseLong(principal.getName());
            } catch (NumberFormatException ignored) { }
        }
        throw new ApplicationErrorException(new ApplicationError("AUTHENTICATION_REQUIRED", "Authentication is required"));
    }

    public static Set<String> roles(@Nullable Principal principal) {
        if (principal instanceof Authentication auth) {
            return auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toSet());
        }
        return Set.of();
    }

    public static long requireAnyRole(@Nullable Principal principal, String... allowedRoles) {
        long userId = requireUserId(principal);
        Set<String> userRoles = roles(principal);
        for (String role : allowedRoles) {
            if (userRoles.contains(role)) return userId;
        }
        throw new ApplicationErrorException(new ApplicationError("FORBIDDEN", "Insufficient role permissions"));
    }
}
