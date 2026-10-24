package com.greenlink.admin.security;

import com.greenlink.core.application.auth.port.TokenProvider;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

@Component
public class JwtProvider implements TokenProvider {

    private final SecretKey key;
    private final long expiration;

    public JwtProvider(
        @Value("${jwt.secret}") String secret,
        @Value("${jwt.expiration}") long expiration
    ) {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            throw new IllegalArgumentException("jwt.secret must be at least 32 bytes");
        }
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.expiration = expiration;
    }

    public String generateToken(Long id, List<String> roles) {
        return generateToken(id, roles, null);
    }

    public String generateToken(Long id, List<String> roles, Long companyId) {
        var builder = Jwts.builder()
            .subject(String.valueOf(id))
            .claim("roles", roles)
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + expiration));
        if (companyId != null) {
            builder.claim("companyId", companyId);
        }
        return builder.signWith(key).compact();
    }

    @SuppressWarnings("unchecked")
    public JwtClaims extractClaims(String token) {
        try {
            var payload = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
            Long companyId = null;
            Object companyIdClaim = payload.get("companyId");
            if (companyIdClaim instanceof Number n) {
                companyId = n.longValue();
            }
            return new JwtClaims(
                payload.getSubject(),
                (List<String>) payload.get("roles"),
                companyId
            );
        } catch (Exception e) {
            return null;
        }
    }

    public record JwtClaims(String subject, List<String> roles, Long companyId) {}
}
