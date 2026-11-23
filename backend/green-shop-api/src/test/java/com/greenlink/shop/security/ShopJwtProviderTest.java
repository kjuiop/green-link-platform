package com.greenlink.shop.security;

import com.greenlink.shopcore.domain.auth.ShopRole;
import com.greenlink.shopcore.domain.auth.TokenScope;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ShopJwtProviderTest {

    private static final String SECRET = "test-secret-test-secret-test-secret-0123";

    private final ShopJwtProvider provider = new ShopJwtProvider(SECRET, 60_000);

    @Test
    void FULL_토큰은_역할과_scope를_담아_파싱된다() {
        String token = provider.issue(7L, ShopRole.SHOP_ADMIN, TokenScope.FULL);

        var principal = provider.parse(token).orElseThrow();

        assertThat(principal.adminId()).isEqualTo(7L);
        assertThat(principal.role()).isEqualTo(ShopRole.SHOP_ADMIN);
        assertThat(principal.scope()).isEqualTo(TokenScope.FULL);
        assertThat(principal.superAdmin()).isFalse();
    }

    @Test
    void 슈퍼관리자_토큰은_superAdmin이다() {
        String token = provider.issue(1L, ShopRole.SUPER_ADMIN, TokenScope.FULL);

        assertThat(provider.parse(token).orElseThrow().superAdmin()).isTrue();
    }

    @Test
    void LIMITED_토큰에는_역할이_없다() {
        String token = provider.issue(7L, null, TokenScope.LIMITED);

        var principal = provider.parse(token).orElseThrow();

        assertThat(principal.role()).isNull();
        assertThat(principal.scope()).isEqualTo(TokenScope.LIMITED);
    }

    @Test
    void 만료된_토큰은_거부된다() {
        ShopJwtProvider expired = new ShopJwtProvider(SECRET, -1_000);

        assertThat(provider.parse(expired.issue(7L, ShopRole.SHOP_ADMIN, TokenScope.FULL))).isEmpty();
    }

    @Test
    void 다른_키로_서명된_토큰은_거부된다() {
        ShopJwtProvider other = new ShopJwtProvider("other-secret-other-secret-other-secret-01", 60_000);

        assertThat(provider.parse(other.issue(7L, ShopRole.SHOP_ADMIN, TokenScope.FULL))).isEmpty();
    }

    @Test
    void 변조되거나_형식이_잘못된_토큰은_거부된다() {
        String token = provider.issue(7L, ShopRole.SHOP_ADMIN, TokenScope.FULL);

        assertThat(provider.parse(token + "x")).isEmpty();
        assertThat(provider.parse("not-a-jwt")).isEmpty();
    }

    @Test
    void 역할_없는_FULL_토큰은_거부된다() {
        String token = Jwts.builder().subject("7").claim("scope", "FULL")
            .expiration(new Date(System.currentTimeMillis() + 60_000))
            .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8))).compact();

        assertThat(provider.parse(token)).isEmpty();
    }

    @Test
    void 시크릿이_32바이트_미만이면_기동에_실패한다() {
        assertThatThrownBy(() -> new ShopJwtProvider("short", 60_000))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
