package com.preciazo.platform.iam.infrastructure.tokens.jwt;

import com.preciazo.platform.iam.application.internal.outboundservices.tokens.TokenService;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.HttpServletRequest;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

@Service
public class JwtTokenService implements TokenService {
    private static final Logger LOG = LoggerFactory.getLogger(JwtTokenService.class);
    private static final String BEARER_PREFIX = "Bearer ";

    private final SecretKey signingKey;
    private final int expirationDays;

    public JwtTokenService(
            @Value("${authorization.jwt.secret}") String secret,
            @Value("${authorization.jwt.expiration.days:7}") int expirationDays) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationDays = expirationDays;
    }

    @Override
    public String generateToken(long userId, String email, List<String> roles) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(email)
                .claim("uid", userId)
                .claim("roles", roles)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(expirationDays, ChronoUnit.DAYS)))
                .signWith(signingKey)
                .compact();
    }

    @Override
    public String getEmailFromToken(String token) {
        return parseClaims(token).getSubject();
    }

    @Override
    public Long getUserIdFromToken(String token) {
        return parseClaims(token).get("uid", Long.class);
    }

    @Override
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (SignatureException | MalformedJwtException | ExpiredJwtException | UnsupportedJwtException | IllegalArgumentException ex) {
            LOG.debug("JWT validation failed: {}", ex.getClass().getSimpleName());
            return false;
        }
    }

    public @Nullable String extractBearerToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith(BEARER_PREFIX)) {
            return header.substring(BEARER_PREFIX.length()).strip();
        }
        return null;
    }

    private Claims parseClaims(String token) {
        return Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token).getPayload();
    }
}
