package com.example.Habits.security;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class JwtUserIdExtractor {

    public Long extract(Jwt jwt){
        Object claim = jwt.getClaim("id");
        if (claim instanceof Number number) {
            return number.longValue();
        }
        if(claim instanceof String value){
            try {
                return Long.parseLong(value);
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException( "Invalid user ID claim");
            }
        }
        throw new IllegalArgumentException( "Missing user ID claim");
    }
}
