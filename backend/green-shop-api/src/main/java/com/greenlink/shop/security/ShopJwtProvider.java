package com.greenlink.shop.security;

import com.greenlink.shopcore.application.auth.port.AccessTokenIssuer;
import com.greenlink.shopcore.domain.auth.ShopRole;
import com.greenlink.shopcore.domain.auth.TokenScope;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;

import io.jsonwebtoken.security.Keys;

@Component
public class ShopJwtProvider implements AccessTokenIssuer {

    private static final String CLAIM_SCOPE = "scope";
    private static final String CLAIM_ROLE = "role";
    private static final String CLAIM_COMPANY_ID = "cid";

    private final SecretKey key;
    private final long expirationMillis;

    public ShopJwtProvider(@Value("${jwt.secret}") String secret,
                           @Value("${jwt.expiration}") long expirationMillis) {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            throw new IllegalArgumentException("jwt.secret must be at least 32 bytes");
        }
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.expirationMillis = expirationMillis;
    }

    @Override
    public String issue(Long adminId, Long companyId, ShopRole role, TokenScope scope) {
        long now = System.currentTimeMillis();
        var builder = Jwts.builder()
            .subject(String.valueOf(adminId))
            .claim(CLAIM_SCOPE, scope.name())
            .issuedAt(new Date(now))
            .expiration(new Date(now + expirationMillis));
        if (companyId != null) {
            builder.claim(CLAIM_COMPANY_ID, companyId);
        }
        if (role != null) {
            builder.claim(CLAIM_ROLE, role.name());
        }
        return builder.signWith(key).compact();
    }

    public Optional<ShopPrincipal> parse(String token) {
        try {
            var payload = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
            TokenScope scope = TokenScope.valueOf(payload.get(CLAIM_SCOPE, String.class));
            String roleClaim = payload.get(CLAIM_ROLE, String.class);
            ShopRole role = roleClaim == null ? null : ShopRole.valueOf(roleClaim);
            if (scope == TokenScope.FULL && role == null) {
                return Optional.empty();
            }
            Number companyClaim = payload.get(CLAIM_COMPANY_ID, Number.class);
            Long companyId = companyClaim == null ? null : companyClaim.longValue();
            return Optional.of(new ShopPrincipal(Long.valueOf(payload.getSubject()), companyId, role, scope));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
