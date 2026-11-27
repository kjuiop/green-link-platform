package com.greenlink.admin.infrastructure.config;

import com.greenlink.admin.security.JwtAuthenticationFilter;
import com.greenlink.admin.security.JwtProvider;
import com.greenlink.core.domain.admin.model.AdminRole;
import com.greenlink.core.domain.company.model.CompanyType;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringJUnitWebConfig(AdminSecurityRulesTest.TestConfig.class)
@TestPropertySource(properties = {
    "jwt.secret=test-secret-test-secret-test-secret-0123",
    "jwt.expiration=60000"
})
class AdminSecurityRulesTest {

    @Configuration
    @EnableWebMvc
    @Import({SecurityConfig.class, JwtAuthenticationFilter.class, JwtProvider.class, CatchAllController.class})
    static class TestConfig {}

    @RestController
    static class CatchAllController {
        @RequestMapping("/**")
        String any() { return "ok"; }
    }

    @Autowired WebApplicationContext context;
    @Autowired FilterChainProxy springSecurityFilterChain;
    @Autowired JwtProvider jwt;

    MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).addFilters(springSecurityFilterChain).build();
    }

    private String bearer(AdminRole role, CompanyType type) {
        return "Bearer " + jwt.generateToken(1L, role, type, type == null ? null : 7L);
    }

    private String superAdmin() { return bearer(AdminRole.SUPER_ADMIN, null); }
    private String ordering() { return bearer(AdminRole.PARTNER_ADMIN, CompanyType.ORDERING); }
    private String vendor() { return bearer(AdminRole.PARTNER_ADMIN, CompanyType.VENDOR); }
    private String both() { return bearer(AdminRole.PARTNER_ADMIN, CompanyType.BOTH); }
    private String brand() { return bearer(AdminRole.BRAND_ADMIN, null); }

    private void expect(String path, String auth, ResultMatcher matcher) throws Exception {
        var req = get(path);
        if (auth != null) req = req.header("Authorization", auth);
        mvc.perform(req).andExpect(matcher);
    }

    private void allowed(String path, String... auths) throws Exception {
        for (String a : auths) expect(path, a, status().isOk());
    }

    private void denied(String path, String... auths) throws Exception {
        for (String a : auths) expect(path, a, status().isForbidden());
    }

    @Test
    void 수주업체_API는_VENDOR_capability가_필요하다() throws Exception {
        allowed("/api/vendor/orders", vendor(), both());
        denied("/api/vendor/orders", null, ordering(), superAdmin(), brand());
    }

    @Test
    void 발주업체_API는_ORDERING_capability가_필요하다() throws Exception {
        allowed("/api/ordering/anything", ordering(), both());
        denied("/api/ordering/anything", null, vendor(), superAdmin(), brand());
    }

    @Test
    void 관리자_API는_슈퍼관리자와_ORDERING만_접근한다() throws Exception {
        allowed("/api/admin/something", superAdmin(), ordering(), both());
        denied("/api/admin/something", null, vendor(), brand());
    }

    @Test
    void 고객주문관리는_슈퍼관리자와_발주사만_접근한다() throws Exception {
        allowed("/api/admin/customer-orders", superAdmin(), ordering(), both());
        allowed("/api/admin/customer-orders/5", superAdmin(), ordering(), both());
        denied("/api/admin/customer-orders", null, vendor(), brand());
    }

    @Test
    void 슈퍼관리자_전용_API는_파트너관리자가_접근할_수_없다() throws Exception {
        allowed("/api/admin/vendor-fees", superAdmin());
        denied("/api/admin/vendor-fees", ordering(), vendor(), both(), brand());
        allowed("/api/base-products", superAdmin());
        denied("/api/base-products", ordering(), both());
    }

    @Test
    void 상품_공급_API는_슈퍼관리자와_수주사가_접근한다() throws Exception {
        allowed("/api/vendor/products", superAdmin(), vendor(), both());
        denied("/api/vendor/products", ordering(), brand());
    }

    @Test
    void 쇼핑몰_API는_슈퍼관리자_발주사_수주사가_접근한다() throws Exception {
        allowed("/api/shop/anything", superAdmin(), ordering(), vendor(), both());
        denied("/api/shop/anything", null, brand());
    }

    @Test
    void 주문과_메뉴_조회는_로그인한_모든_관리자가_접근한다() throws Exception {
        allowed("/api/orders", superAdmin(), ordering(), vendor(), both());
        allowed("/api/menus/by-role", superAdmin(), ordering(), vendor(), both());
        denied("/api/orders", (String) null);
    }

    @Test
    void 예전_roles_클레임_토큰은_인증되지_않는다() throws Exception {
        String legacy = "Bearer " + Jwts.builder()
            .subject("1")
            .claim("roles", java.util.List.of("SUPER_ADMIN"))
            .expiration(new Date(System.currentTimeMillis() + 60_000))
            .signWith(Keys.hmacShaKeyFor("test-secret-test-secret-test-secret-0123".getBytes(StandardCharsets.UTF_8)))
            .compact();
        denied("/api/admin/something", legacy);
        denied("/api/orders", legacy);
    }

    @Test
    void 알_수_없는_역할_클레임_토큰은_인증되지_않는다() throws Exception {
        String unknown = "Bearer " + Jwts.builder()
            .subject("1")
            .claim("role", "ADMIN")
            .expiration(new Date(System.currentTimeMillis() + 60_000))
            .signWith(Keys.hmacShaKeyFor("test-secret-test-secret-test-secret-0123".getBytes(StandardCharsets.UTF_8)))
            .compact();
        denied("/api/orders", unknown);
    }
}
