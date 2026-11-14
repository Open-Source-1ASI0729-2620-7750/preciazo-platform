package com.preciazo.platform.shopping.interfaces.rest.security;

import com.preciazo.platform.shared.application.result.*;
import org.jspecify.annotations.Nullable;
import java.security.Principal;

/** Uses only a server-supplied principal; never accepts an owner header/body or an anonymous default. */
public final class CurrentBuyer {
    private CurrentBuyer() { }
    public static long requireId(@Nullable Principal principal) {
        if (principal != null && principal.getName() != null && principal.getName().matches("[1-9][0-9]*")) {
            try { return Long.parseLong(principal.getName()); } catch (NumberFormatException ignored) { }
        }
        throw new ApplicationErrorException(new ApplicationError("AUTHENTICATION_REQUIRED", "Authentication is required"));
    }
}
