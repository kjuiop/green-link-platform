package com.greenlink.admin.security;

import com.greenlink.core.application.auth.port.TokenProvider;
import com.greenlink.core.domain.admin.model.AdminRole;
import com.greenlink.core.domain.company.model.CompanyType;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

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

    @Override
    public String generateToken(Long id, AdminRole role, CompanyType companyType, Long companyId) {
        var builder = Jwts.builder()
            .subject(String.valueOf(id))
            .claim("role", role.name())
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + expiration));
        if (companyType != null) {
            builder.claim("companyType", companyType.name());
        }
        if (companyId != null) {
            builder.claim("companyId", companyId);
        }
        return builder.signWith(key).compact();
    }

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
            Object roleClaim = payload.get("role");
            if (!(roleClaim instanceof String roleName)) {
                return null;
            }
            Object companyTypeClaim = payload.get("companyType");
            CompanyType companyType = companyTypeClaim instanceof String t ? CompanyType.valueOf(t) : null;
            return new JwtClaims(payload.getSubject(), AdminRole.valueOf(roleName), companyType, companyId);
        } catch (Exception e) {
            return null;
        }
    }

    public record JwtClaims(String subject, AdminRole role, CompanyType companyType, Long companyId) {}
}
