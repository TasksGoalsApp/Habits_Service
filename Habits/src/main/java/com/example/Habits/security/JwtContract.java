package com.example.Habits.security;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.security.oauth2.core.*;
import org.springframework.security.oauth2.jwt.Jwt;

/** Shared wire contract; keep identical across the independently built services. */
public final class JwtContract implements OAuth2TokenValidator<Jwt> {
    private final String audience;

    public JwtContract(String audience) { this.audience = audience; }

    public static long userId(Jwt jwt) {
        Object value = jwt.getClaims().get("id");
        if (!(value instanceof Number number)) throw new IllegalArgumentException("Numeric id required");
        long id = new BigDecimal(number.toString()).longValueExact();
        if (id <= 0) throw new IllegalArgumentException("Positive id required");
        return id;
    }

    @Override
    public OAuth2TokenValidatorResult validate(Jwt jwt) {
        try {
            userId(jwt);
            Object roles = jwt.getClaims().get("roles");
            if (jwt.getSubject() == null || jwt.getSubject().isBlank()
                    || jwt.getExpiresAt() == null || jwt.getIssuedAt() == null
                    || !jwt.getExpiresAt().isAfter(jwt.getIssuedAt())
                    || !jwt.getAudience().contains(audience)
                    || !(roles instanceof List<?> values) || values.isEmpty()
                    || values.stream().anyMatch(role -> !(role instanceof String text)
                            || !text.matches("[A-Z][A-Z_]*"))) {
                return invalid();
            }
            return OAuth2TokenValidatorResult.success();
        } catch (RuntimeException exception) {
            return invalid();
        }
    }

    private OAuth2TokenValidatorResult invalid() {
        return OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", "Invalid access token claims", null));
    }
}
