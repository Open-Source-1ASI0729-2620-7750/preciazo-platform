package com.preciazo.platform.iam.application.internal.outboundservices.tokens;

import java.util.List;

public interface TokenService {
    String generateToken(long userId, String email, List<String> roles);
    String getEmailFromToken(String token);
    Long getUserIdFromToken(String token);
    boolean validateToken(String token);
}
