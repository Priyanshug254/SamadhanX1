package com.samadhanx.module.auth.service;

import com.samadhanx.infrastructure.security.UserPrincipal;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Service for generating, parsing, and validating JWT tokens (JJWT 0.12).
 */
@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    @Value("${samadhanx.jwt.secret}")
    private String jwtSecret;

    @Value("${samadhanx.jwt.expiry-ms:86400000}")
    private long jwtExpiryMs;

    @Value("${samadhanx.supabase.issuer:}")
    private String supabaseIssuer;

    private SecretKey signingKey;

    @PostConstruct
    public void init() {
        if (jwtSecret == null || jwtSecret.trim().length() < 32) {
            throw new IllegalArgumentException("JWT secret must be at least 32 characters (256 bits) long for HMAC-SHA256");
        }
        this.signingKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(UserPrincipal userPrincipal) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("userId", userPrincipal.getId().toString());
        extraClaims.put("email", userPrincipal.getEmail());
        extraClaims.put("firstName", userPrincipal.getFirstName());
        extraClaims.put("lastName", userPrincipal.getLastName());

        List<String> roles = userPrincipal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        extraClaims.put("roles", roles);

        return buildToken(extraClaims, userPrincipal.getUsername(), jwtExpiryMs);
    }

    public String generateTokenForUser(UUID userId, String email, String firstName, String lastName, List<String> roles) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("userId", userId.toString());
        extraClaims.put("email", email);
        extraClaims.put("firstName", firstName);
        extraClaims.put("lastName", lastName);
        extraClaims.put("roles", roles);

        return buildToken(extraClaims, email, jwtExpiryMs);
    }

    private String buildToken(Map<String, Object> extraClaims, String subject, long expirationMs) {
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);
        Date expiryDate = new Date(nowMillis + expirationMs);

        return Jwts.builder()
                .claims(extraClaims)
                .subject(subject)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            log.warn("Invalid JWT signature/format: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.warn("Expired JWT token: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.warn("Unsupported JWT token: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("JWT claims string is empty or null: {}", e.getMessage());
        }
        return false;
    }

    /**
     * Validates an access token issued by this application's configured
     * Supabase project. Supabase signs legacy-project access tokens using the
     * project's JWT secret; no application token is generated or accepted.
     */
    public boolean validateSupabaseToken(String token) {
        try {
            var parser = Jwts.parser().verifyWith(signingKey);
            if (supabaseIssuer != null && !supabaseIssuer.isBlank()) {
                parser.requireIssuer(supabaseIssuer);
            }
            parser.build().parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            log.warn("Invalid Supabase access token: {}", e.getMessage());
            return false;
        }
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public UUID extractUserId(String token) {
        Claims claims = extractAllClaims(token);
        String userIdStr = claims.get("userId", String.class);
        return userIdStr != null ? UUID.fromString(userIdStr) : null;
    }

    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("roles", List.class);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public long getExpiryMs() {
        return jwtExpiryMs;
    }
}
