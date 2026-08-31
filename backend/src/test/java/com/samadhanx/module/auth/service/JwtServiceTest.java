package com.samadhanx.module.auth.service;

import com.samadhanx.infrastructure.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JwtService Unit Tests")
class JwtServiceTest {

    private JwtService jwtService;
    private final String secret = "test-secret-key-which-is-at-least-32-characters-long-for-hs256";
    private final long expiryMs = 3600000; // 1 hour

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "jwtSecret", secret);
        ReflectionTestUtils.setField(jwtService, "jwtExpiryMs", expiryMs);
        jwtService.init();
    }

    @Test
    @DisplayName("Should generate valid JWT token for UserPrincipal")
    void shouldGenerateValidToken() {
        UUID userId = UUID.randomUUID();
        UserPrincipal principal = UserPrincipal.builder()
                .id(userId)
                .email("test.citizen@samadhanx.org")
                .firstName("Test")
                .lastName("Citizen")
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_CITIZEN")))
                .active(true)
                .emailVerified(true)
                .build();

        String token = jwtService.generateToken(principal);

        assertNotNull(token);
        assertTrue(jwtService.validateToken(token));
        assertEquals("test.citizen@samadhanx.org", jwtService.extractUsername(token));
        assertEquals(userId, jwtService.extractUserId(token));
        List<String> roles = jwtService.extractRoles(token);
        assertNotNull(roles);
        assertTrue(roles.contains("ROLE_CITIZEN"));
    }

    @Test
    @DisplayName("Should reject invalid or malformed token")
    void shouldRejectInvalidToken() {
        assertFalse(jwtService.validateToken("invalid.token.structure"));
        assertFalse(jwtService.validateToken(""));
        assertFalse(jwtService.validateToken(null));
    }

    @Test
    @DisplayName("Should reject token signed with different secret")
    void shouldRejectTokenWithWrongSecret() {
        JwtService anotherService = new JwtService();
        ReflectionTestUtils.setField(anotherService, "jwtSecret", "another-completely-different-secret-key-32-chars");
        ReflectionTestUtils.setField(anotherService, "jwtExpiryMs", expiryMs);
        anotherService.init();

        UUID userId = UUID.randomUUID();
        UserPrincipal principal = UserPrincipal.builder()
                .id(userId)
                .email("test@samadhanx.org")
                .firstName("Test")
                .lastName("User")
                .authorities(List.of())
                .active(true)
                .build();

        String tokenFromOther = anotherService.generateToken(principal);

        assertFalse(jwtService.validateToken(tokenFromOther));
    }

    @Test
    @DisplayName("Should reject expired token")
    void shouldRejectExpiredToken() {
        JwtService expiredJwtService = new JwtService();
        ReflectionTestUtils.setField(expiredJwtService, "jwtSecret", secret);
        ReflectionTestUtils.setField(expiredJwtService, "jwtExpiryMs", -1000L); // Already expired
        expiredJwtService.init();

        UUID userId = UUID.randomUUID();
        UserPrincipal principal = UserPrincipal.builder()
                .id(userId)
                .email("expired@samadhanx.org")
                .firstName("Expired")
                .lastName("User")
                .authorities(List.of())
                .active(true)
                .build();

        String expiredToken = expiredJwtService.generateToken(principal);

        assertFalse(jwtService.validateToken(expiredToken));
    }
}
