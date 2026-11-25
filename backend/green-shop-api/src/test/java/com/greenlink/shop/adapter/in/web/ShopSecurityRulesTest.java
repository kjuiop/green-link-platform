package com.greenlink.shop.adapter.in.web;

import com.greenlink.shop.global.exception.GlobalExceptionHandler;
import com.greenlink.shop.infrastructure.config.SecurityConfig;
import com.greenlink.shop.security.ShopJwtProvider;
import com.greenlink.shopcore.application.access.dto.MyAccess;
import com.greenlink.shopcore.application.access.usecase.AccessRequestUseCase;
import com.greenlink.shopcore.application.admin.usecase.ShopAdminManagementUseCase;
import com.greenlink.shopcore.application.auth.dto.ShopSession;
import com.greenlink.shopcore.application.auth.usecase.RefreshTokenUseCase;
import com.greenlink.shopcore.application.auth.usecase.ShopLoginUseCase;
import com.greenlink.shopcore.application.menu.usecase.GetMenusUseCase;
import com.greenlink.shopcore.domain.auth.AccessStatus;
import com.greenlink.shopcore.domain.auth.ShopRole;
import com.greenlink.shopcore.domain.auth.TokenScope;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringJUnitWebConfig(ShopSecurityRulesTest.TestConfig.class)
@TestPropertySource(properties = {
    "jwt.secret=test-secret-test-secret-test-secret-0123",
    "jwt.expiration=60000"
})
class ShopSecurityRulesTest {

    @Configuration
    @EnableWebMvc
    @Import({SecurityConfig.class, ShopJwtProvider.class, GlobalExceptionHandler.class,
        ShopAuthController.class, AccessRequestController.class, ShopAdminController.class, MenuController.class})
    static class TestConfig {
        @Bean ShopLoginUseCase shopLoginUseCase() { return mock(ShopLoginUseCase.class); }
        @Bean RefreshTokenUseCase refreshTokenUseCase() { return mock(RefreshTokenUseCase.class); }
        @Bean AccessRequestUseCase accessRequestUseCase() { return mock(AccessRequestUseCase.class); }
        @Bean ShopAdminManagementUseCase shopAdminManagementUseCase() { return mock(ShopAdminManagementUseCase.class); }
        @Bean GetMenusUseCase getMenusUseCase() { return mock(GetMenusUseCase.class); }
    }

    @Autowired WebApplicationContext context;
    @Autowired FilterChainProxy springSecurityFilterChain;
    @Autowired ShopJwtProvider jwt;
    @Autowired ShopLoginUseCase loginUseCase;
    @Autowired RefreshTokenUseCase refreshUseCase;
    @Autowired AccessRequestUseCase accessRequestUseCase;
    @Autowired ShopAdminManagementUseCase managementUseCase;
    @Autowired GetMenusUseCase menusUseCase;

    MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).addFilters(springSecurityFilterChain).build();
        when(menusUseCase.getMenus(any())).thenReturn(List.of());
        when(managementUseCase.list(any())).thenReturn(List.of());
        when(accessRequestUseCase.getMyAccess(anyLong(), anyBoolean()))
            .thenReturn(new MyAccess(AccessStatus.PENDING, null));
        when(accessRequestUseCase.apply(anyLong(), anyBoolean()))
            .thenReturn(new MyAccess(AccessStatus.PENDING, null));
        when(refreshUseCase.refresh(anyLong())).thenReturn(
            new ShopSession("t", "shop01", "홍길동", 7L, "업체", ShopRole.PARTNER_ADMIN, AccessStatus.APPROVED));
        when(loginUseCase.login(anyString(), anyString())).thenReturn(
            new ShopSession("t", "shop01", "홍길동", 7L, "업체", null, AccessStatus.NONE));
    }

    private String limited() { return "Bearer " + jwt.issue(5L, 7L, null, TokenScope.LIMITED); }
    private String partnerAdmin() { return "Bearer " + jwt.issue(5L, 7L, ShopRole.PARTNER_ADMIN, TokenScope.FULL); }
    private String brandAdmin() { return "Bearer " + jwt.issue(6L, 7L, ShopRole.BRAND_ADMIN, TokenScope.FULL); }
    private String superAdmin() { return "Bearer " + jwt.issue(1L, null, ShopRole.SUPER_ADMIN, TokenScope.FULL); }

    @Test
    void 로그인과_ping은_토큰없이_가능하다() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"loginId\":\"shop01\",\"password\":\"pw\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessStatus").value("NONE"))
            .andExpect(jsonPath("$.role").isEmpty())
            .andExpect(jsonPath("$.companyId").value(7));
    }

    @Test
    void 토큰이_없으면_보호된_API는_401_JSON() throws Exception {
        mvc.perform(get("/api/me")).andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value("SHOP_AUTH_401"));
        mvc.perform(get("/api/menus")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/auth/refresh")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/admin/shop-admins")).andExpect(status().isUnauthorized());
    }

    @Test
    void 잘못된_토큰은_401() throws Exception {
        mvc.perform(get("/api/me").header("Authorization", "Bearer garbage")).andExpect(status().isUnauthorized());
    }

    @Test
    void LIMITED는_me_신청_갱신만_가능하다() throws Exception {
        mvc.perform(get("/api/me").header("Authorization", limited()))
            .andExpect(status().isOk()).andExpect(jsonPath("$.accessStatus").value("PENDING"));
        mvc.perform(post("/api/access-requests").header("Authorization", limited())).andExpect(status().isOk());
        mvc.perform(post("/api/auth/refresh").header("Authorization", limited())).andExpect(status().isOk());
        verify(refreshUseCase).refresh(5L);
    }

    @Test
    void LIMITED는_메뉴와_관리자_API가_403() throws Exception {
        mvc.perform(get("/api/menus").header("Authorization", limited()))
            .andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("SHOP_AUTH_403"));
        mvc.perform(get("/api/admin/shop-admins").header("Authorization", limited())).andExpect(status().isForbidden());
        mvc.perform(post("/api/admin/shop-admins/1/approve").header("Authorization", limited()))
            .andExpect(status().isForbidden());
    }

    @Test
    void FULL_PARTNER_ADMIN은_메뉴는_가능하고_관리자_API는_403() throws Exception {
        mvc.perform(get("/api/menus").header("Authorization", partnerAdmin())).andExpect(status().isOk());
        mvc.perform(get("/api/admin/shop-admins").header("Authorization", partnerAdmin()))
            .andExpect(status().isForbidden());
        mvc.perform(post("/api/admin/shop-admins/1/reject").header("Authorization", partnerAdmin())
                .contentType(MediaType.APPLICATION_JSON).content("{\"reason\":\"x\"}"))
            .andExpect(status().isForbidden());
    }

    @Test
    void FULL_BRAND_ADMIN도_메뉴는_가능하고_관리자_API는_403() throws Exception {
        mvc.perform(get("/api/menus").header("Authorization", brandAdmin())).andExpect(status().isOk());
        mvc.perform(get("/api/admin/shop-admins").header("Authorization", brandAdmin()))
            .andExpect(status().isForbidden());
        mvc.perform(post("/api/admin/shop-admins/1/approve").header("Authorization", brandAdmin()))
            .andExpect(status().isForbidden());
        verify(managementUseCase, never()).approve(anyLong(), anyLong());
    }

    @Test
    void SUPER_ADMIN은_관리자_API를_사용하고_처리자는_토큰_subject다() throws Exception {
        mvc.perform(get("/api/admin/shop-admins").header("Authorization", superAdmin())).andExpect(status().isOk());
        mvc.perform(post("/api/admin/shop-admins/9/approve").header("Authorization", superAdmin()))
            .andExpect(status().isNoContent());
        mvc.perform(post("/api/admin/shop-admins/9/reject").header("Authorization", superAdmin())
                .contentType(MediaType.APPLICATION_JSON).content("{\"reason\":\"서류 미비\"}"))
            .andExpect(status().isNoContent());
        verify(managementUseCase).approve(9L, 1L);
        verify(managementUseCase).reject(9L, "서류 미비", 1L);
    }

    @Test
    void 거절_사유가_비어있으면_400() throws Exception {
        mvc.perform(post("/api/admin/shop-admins/9/reject").header("Authorization", superAdmin())
                .contentType(MediaType.APPLICATION_JSON).content("{\"reason\":\" \"}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void SUPER_ADMIN_토큰의_me는_superAdmin으로_전달된다() throws Exception {
        mvc.perform(get("/api/me").header("Authorization", superAdmin())).andExpect(status().isOk());
        verify(accessRequestUseCase).getMyAccess(1L, true);
    }
}
