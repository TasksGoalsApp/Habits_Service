package com.example.Habits;

import java.time.Instant;
import java.util.*;
import java.util.function.Consumer;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.*;

final class TestTokens {
    static final String SECRET = "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=";
    static String token(long id, String role) throws Exception { return token(id, role, b -> {}); }
    static String token(long id, String role, Consumer<JWTClaimsSet.Builder> changes) throws Exception {
        var now = Instant.now();
        var claims = new JWTClaimsSet.Builder().subject("tester").issuer("daily-dojo-user-service")
                .audience("daily-dojo-api").claim("id", id).claim("roles", List.of(role))
                .issueTime(Date.from(now)).expirationTime(Date.from(now.plusSeconds(3600)));
        changes.accept(claims);
        var token = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims.build());
        token.sign(new MACSigner(Base64.getDecoder().decode(SECRET)));
        return token.serialize();
    }
}
