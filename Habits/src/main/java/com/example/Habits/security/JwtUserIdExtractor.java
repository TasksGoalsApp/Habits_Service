package com.example.Habits.security;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class JwtUserIdExtractor {

    public Long extract(Jwt jwt){
        return JwtContract.userId(jwt);
    }
}
