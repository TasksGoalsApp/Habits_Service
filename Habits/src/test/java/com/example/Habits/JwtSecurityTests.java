package com.example.Habits;

import java.time.Instant;
import java.util.Date;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class JwtSecurityTests {
    @Autowired MockMvc mvc;

    @Test void validContractTokenIsAccepted() throws Exception {
        mvc.perform(get("/habits").header("Authorization", "Bearer " + TestTokens.token(1, "CUSTOMER")))
                .andExpect(status().isOk());
    }
    @Test void missingAndMalformedTokensAreUnauthorized() throws Exception {
        mvc.perform(get("/habits")).andExpect(status().isUnauthorized());
        mvc.perform(get("/habits").header("Authorization", "Bearer nonsense")).andExpect(status().isUnauthorized());
    }
    @Test void expiredTokenIsUnauthorized() throws Exception {
        var token = TestTokens.token(1, "CUSTOMER", b -> b.issueTime(Date.from(Instant.now().minusSeconds(600)))
                .expirationTime(Date.from(Instant.now().minusSeconds(120))));
        mvc.perform(get("/habits").header("Authorization", "Bearer " + token)).andExpect(status().isUnauthorized());
    }
    @Test void invalidContractsAreUnauthorized() throws Exception {
        for (var token : new String[] {
                TestTokens.token(1,"CUSTOMER", b -> b.issuer("wrong")),
                TestTokens.token(1,"CUSTOMER", b -> b.audience("wrong")),
                TestTokens.token(1,"CUSTOMER", b -> b.claim("id", null).claim("userId", 1)),
                TestTokens.token(1,"CUSTOMER", b -> b.claim("id", "1")),
                TestTokens.token(1,"CUSTOMER", b -> b.claim("id", 1.5)),
                TestTokens.token(1,"CUSTOMER", b -> b.claim("roles", null).claim("role", "CUSTOMER")),
                TestTokens.token(1,"CUSTOMER", b -> b.expirationTime(null)),
                TestTokens.token(1,"CUSTOMER", b -> b.claim("roles", "CUSTOMER"))}) {
            mvc.perform(get("/habits").header("Authorization", "Bearer " + token)).andExpect(status().isUnauthorized());
        }
    }
    @Test void wrongSignatureIsUnauthorized() throws Exception {
        var parts = TestTokens.token(1, "CUSTOMER").split("\\.");
        var tampered = parts[0] + "." + parts[1] + "." + (parts[2].startsWith("A") ? "B" : "A") + parts[2].substring(1);
        mvc.perform(get("/habits").header("Authorization", "Bearer " + tampered)).andExpect(status().isUnauthorized());
    }
    @Test void corsAllowsOnlyConfiguredOrigin() throws Exception {
        mvc.perform(options("/habits").header("Origin","http://localhost:5173")
                .header("Access-Control-Request-Method","GET").header("Access-Control-Request-Headers","authorization"))
                .andExpect(status().isOk()).andExpect(header().string("Access-Control-Allow-Origin","http://localhost:5173"));
        mvc.perform(options("/habits").header("Origin","https://untrusted.example")
                .header("Access-Control-Request-Method","GET")).andExpect(status().isForbidden());
    }
}
