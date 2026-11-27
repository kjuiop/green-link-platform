package com.greenlink.shop.adapter.in.web;

import com.greenlink.shop.global.exception.GlobalExceptionHandler;
import com.greenlink.shop.infrastructure.config.SecurityConfig;
import com.greenlink.shop.security.ShopJwtProvider;
import com.greenlink.shopcore.application.access.dto.MyAccess;
import com.greenlink.shopcore.application.access.usecase.AccessRequestUseCase;
import com.greenlink.shopcore.application.admin.usecase.ShopAdminManagementUseCase;
import com.greenlink.shopcore.application.auth.dto.ShopSession;
import com.greenlink.shopcore.application.brand.usecase.BrandManagementUseCase;
import com.greenlink.shopcore.application.admin.dto.CreateMemberCommand;
import com.greenlink.shopcore.application.common.ManagementActor;
import com.greenlink.shopcore.domain.brand.model.ShopBrand;
import com.greenlink.shopcore.application.auth.dto.SignupResult;
import com.greenlink.shopcore.application.auth.usecase.ShopPasswordSetupUseCase;
import com.greenlink.shopcore.application.auth.usecase.ShopSignupUseCase;
import com.greenlink.shopcore.global.exception.ShopBusinessException;
import com.greenlink.shopcore.global.exception.ShopErrorCode;
import com.greenlink.shopcore.application.auth.usecase.RefreshTokenUseCase;
import com.greenlink.shopcore.application.auth.usecase.ShopLoginUseCase;
import com.greenlink.shopcore.application.menu.usecase.GetMenusUseCase;
import com.greenlink.shopcore.application.onurim.dto.OnurimApplicationStatus;
import com.greenlink.shopcore.application.onurim.dto.OnurimApplicationView;
import com.greenlink.shopcore.application.onurim.dto.OnurimCompanyType;
import com.greenlink.shopcore.application.onurim.usecase.OnurimApplicationUseCase;
import com.greenlink.shopcore.domain.auth.AccessRequestType;
import com.greenlink.shopcore.domain.auth.AccessStatus;
import com.greenlink.shopcore.domain.auth.ShopRole;
import com.greenlink.shopcore.domain.auth.TokenScope;
import com.greenlink.shopcore.domain.member.model.ShopAdminStatus;
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
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
        ShopAuthController.class, ShopSignupController.class, ShopPasswordSetupController.class, ClientIpResolver.class, AccessRequestController.class, MemberController.class, BrandController.class, MenuController.class, OnurimApplicationController.class})
    static class TestConfig {
        @Bean ShopLoginUseCase shopLoginUseCase() { return mock(ShopLoginUseCase.class); }
        @Bean RefreshTokenUseCase refreshTokenUseCase() { return mock(RefreshTokenUseCase.class); }
        @Bean AccessRequestUseCase accessRequestUseCase() { return mock(AccessRequestUseCase.class); }
        @Bean ShopAdminManagementUseCase shopAdminManagementUseCase() { return mock(ShopAdminManagementUseCase.class); }
        @Bean BrandManagementUseCase brandManagementUseCase() { return mock(BrandManagementUseCase.class); }
        @Bean GetMenusUseCase getMenusUseCase() { return mock(GetMenusUseCase.class); }
        @Bean ShopSignupUseCase shopSignupUseCase() { return mock(ShopSignupUseCase.class); }
        @Bean ShopPasswordSetupUseCase shopPasswordSetupUseCase() { return mock(ShopPasswordSetupUseCase.class); }
        @Bean OnurimApplicationUseCase onurimApplicationUseCase() { return mock(OnurimApplicationUseCase.class); }
    }

    @Autowired WebApplicationContext context;
    @Autowired FilterChainProxy springSecurityFilterChain;
    @Autowired ShopJwtProvider jwt;
    @Autowired ShopLoginUseCase loginUseCase;
    @Autowired RefreshTokenUseCase refreshUseCase;
    @Autowired AccessRequestUseCase accessRequestUseCase;
    @Autowired ShopAdminManagementUseCase managementUseCase;
    @Autowired BrandManagementUseCase brandUseCase;
    @Autowired GetMenusUseCase menusUseCase;
    @Autowired ShopSignupUseCase signupUseCase;
    @Autowired ShopPasswordSetupUseCase passwordSetupUseCase;
    @Autowired OnurimApplicationUseCase onurimUseCase;

    MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).addFilters(springSecurityFilterChain).build();
        org.mockito.Mockito.reset(signupUseCase, managementUseCase, brandUseCase, passwordSetupUseCase, onurimUseCase);
        when(menusUseCase.getMenus(any())).thenReturn(List.of());
        when(managementUseCase.list(any(), any(), any())).thenReturn(List.of());
        when(brandUseCase.list(any(), any())).thenReturn(List.of());
        when(accessRequestUseCase.getMyAccess(anyLong(), anyBoolean()))
            .thenReturn(new MyAccess(AccessStatus.PENDING, null, AccessRequestType.COMPANY, ShopRole.PARTNER_ADMIN, java.util.Set.of()));
        when(accessRequestUseCase.apply(anyLong(), anyBoolean(), any(), any()))
            .thenReturn(new MyAccess(AccessStatus.PENDING, null, AccessRequestType.COMPANY, ShopRole.PARTNER_ADMIN, java.util.Set.of()));
        when(refreshUseCase.refresh(anyLong())).thenReturn(
            new ShopSession("t", "shop01", "홍길동", 7L, "업체", ShopRole.PARTNER_ADMIN, AccessStatus.APPROVED));
        when(loginUseCase.login(anyString(), anyString())).thenReturn(
            new ShopSession("t", "shop01", "홍길동", 7L, "업체", null, AccessStatus.NONE));
    }

    private static final String SIGNUP_BODY = "{\"loginId\":\"shop01\",\"password\":\"Passw0rd!\",\"name\":\"홍길동\","
        + "\"phone\":\"010-1234-5678\",\"businessNo\":\"123-45-67890\",\"companyName\":\"꽃집\"}";

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
        mvc.perform(get("/api/members")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/brands")).andExpect(status().isUnauthorized());
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
        mvc.perform(get("/api/access-requests/brands").header("Authorization", limited())).andExpect(status().isOk());
        mvc.perform(get("/api/access-requests/brands")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/auth/refresh").header("Authorization", limited())).andExpect(status().isOk());
        verify(refreshUseCase).refresh(5L);
    }

    @Test
    void LIMITED는_메뉴와_멤버_브랜드_API가_403() throws Exception {
        mvc.perform(get("/api/menus").header("Authorization", limited()))
            .andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("SHOP_AUTH_403"));
        mvc.perform(get("/api/members").header("Authorization", limited())).andExpect(status().isForbidden());
        mvc.perform(post("/api/members/1/approve").header("Authorization", limited()))
            .andExpect(status().isForbidden());
        mvc.perform(get("/api/brands").header("Authorization", limited())).andExpect(status().isForbidden());
        mvc.perform(delete("/api/members/1").header("Authorization", limited())).andExpect(status().isForbidden());
        verifyNoInteractions(managementUseCase, brandUseCase);
    }

    @Test
    void FULL_토큰은_메뉴와_멤버_브랜드_API에_진입하고_권한_판단은_서비스에_맡긴다() throws Exception {
        mvc.perform(get("/api/menus").header("Authorization", partnerAdmin())).andExpect(status().isOk());
        mvc.perform(get("/api/members").header("Authorization", partnerAdmin())).andExpect(status().isOk());
        mvc.perform(get("/api/brands").header("Authorization", brandAdmin())).andExpect(status().isOk());
        verify(managementUseCase).list(new ManagementActor(5L, 7L, ShopRole.PARTNER_ADMIN), null, null);
        verify(brandUseCase).list(new ManagementActor(6L, 7L, ShopRole.BRAND_ADMIN), null);
    }

    @Test
    void 서비스가_거부하면_403_코드가_그대로_내려간다() throws Exception {
        doThrow(new ShopBusinessException(ShopErrorCode.ACCESS_MANAGE_FORBIDDEN))
            .when(managementUseCase).approve(any(), anyLong(), any(), any());

        mvc.perform(post("/api/members/1/approve").header("Authorization", brandAdmin()))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.code").value("SHOP_ACCESS_012"));
    }

    @Test
    void 이전_관리자_경로는_더_이상_열려있지_않다() throws Exception {
        mvc.perform(get("/api/admin/shop-admins").header("Authorization", partnerAdmin()))
            .andExpect(status().isForbidden());
    }

    @Test
    void SUPER_ADMIN_멤버_API_호출은_토큰으로_만든_행위자와_함께_전달된다() throws Exception {
        ManagementActor superActor = new ManagementActor(1L, null, ShopRole.SUPER_ADMIN);

        mvc.perform(get("/api/members").header("Authorization", superAdmin())).andExpect(status().isOk());
        mvc.perform(post("/api/members/9/approve").header("Authorization", superAdmin()))
            .andExpect(status().isNoContent());
        mvc.perform(post("/api/members/9/reject").header("Authorization", superAdmin())
                .contentType(MediaType.APPLICATION_JSON).content("{\"reason\":\"서류 미비\"}"))
            .andExpect(status().isNoContent());

        verify(managementUseCase).list(superActor, null, null);
        verify(managementUseCase).approve(superActor, 9L, null, null);
        verify(managementUseCase).reject(superActor, 9L, "서류 미비");
    }

    @Test
    void 목록_상태_필터가_전달된다() throws Exception {
        mvc.perform(get("/api/members?status=PENDING").header("Authorization", superAdmin()))
            .andExpect(status().isOk());
        verify(managementUseCase).list(new ManagementActor(1L, null, ShopRole.SUPER_ADMIN), ShopAdminStatus.PENDING, null);
        mvc.perform(get("/api/members?companyId=8").header("Authorization", superAdmin())).andExpect(status().isOk());
        verify(managementUseCase).list(new ManagementActor(1L, null, ShopRole.SUPER_ADMIN), null, 8L);
    }

    @Test
    void 거절_사유가_비어있으면_400() throws Exception {
        mvc.perform(post("/api/members/9/reject").header("Authorization", superAdmin())
                .contentType(MediaType.APPLICATION_JSON).content("{\"reason\":\" \"}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void SUPER_ADMIN_토큰의_me는_superAdmin으로_전달된다() throws Exception {
        mvc.perform(get("/api/me").header("Authorization", superAdmin())).andExpect(status().isOk());
        verify(accessRequestUseCase).getMyAccess(1L, true);
    }

    @Test
    void 가입과_사업자번호_조회는_토큰없이_가능하다() throws Exception {
        when(signupUseCase.signup(any(), anyString())).thenReturn(new SignupResult(10L, true));
        when(signupUseCase.businessNoExists(anyString(), anyString())).thenReturn(true);

        mvc.perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON).content(SIGNUP_BODY))
            .andExpect(status().isCreated()).andExpect(jsonPath("$.founder").value(true));
        mvc.perform(get("/api/auth/business-numbers/123-45-67890/exists"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.exists").value(true));
    }

    @Test
    void 가입_입력이_규칙에_맞지_않으면_400이고_서비스를_호출하지_않는다() throws Exception {
        mvc.perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON)
                .content(SIGNUP_BODY.replace("Passw0rd!", "short")))
            .andExpect(status().isBadRequest());
        mvc.perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON)
                .content(SIGNUP_BODY.replace("shop01", "a b")))
            .andExpect(status().isBadRequest());
        verify(signupUseCase, never()).signup(any(), anyString());
    }

    @Test
    void 한도_초과는_429로_매핑된다() throws Exception {
        when(signupUseCase.signup(any(), anyString()))
            .thenThrow(new ShopBusinessException(ShopErrorCode.SIGNUP_RATE_LIMITED));

        mvc.perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON).content(SIGNUP_BODY))
            .andExpect(status().isTooManyRequests())
            .andExpect(jsonPath("$.code").value("SHOP_SIGNUP_001"));
    }

    @Test
    void 시크릿이_없으면_X_Shop_Client_Ip를_믿지_않고_원격_주소를_쓴다() throws Exception {
        when(signupUseCase.signup(any(), anyString())).thenReturn(new SignupResult(10L, true));

        mvc.perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON).content(SIGNUP_BODY)
            .header("X-Shop-Client-Ip", "9.9.9.9").header("X-Shop-Proxy-Secret", "anything"));

        verify(signupUseCase).signup(any(), org.mockito.ArgumentMatchers.eq("127.0.0.1"));
    }

    @Test
    void 승인_요청_본문의_역할과_브랜드가_유스케이스로_전달된다() throws Exception {
        mvc.perform(post("/api/members/9/approve").header("Authorization", partnerAdmin())
                .contentType(MediaType.APPLICATION_JSON).content("{\"role\":\"BRAND_ADMIN\",\"brandIds\":[3,4]}"))
            .andExpect(status().isNoContent());
        verify(managementUseCase).approve(new ManagementActor(5L, 7L, ShopRole.PARTNER_ADMIN), 9L,
            ShopRole.BRAND_ADMIN, Set.of(3L, 4L));
    }

    @Test
    void 알_수_없는_역할로_승인하면_400() throws Exception {
        mvc.perform(post("/api/members/9/approve").header("Authorization", superAdmin())
                .contentType(MediaType.APPLICATION_JSON).content("{\"role\":\"NOPE\"}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void 역할_변경은_본문의_역할이_필수다() throws Exception {
        mvc.perform(patch("/api/members/9/role").header("Authorization", superAdmin())
                .contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isBadRequest());
        mvc.perform(patch("/api/members/9/role").header("Authorization", superAdmin())
                .contentType(MediaType.APPLICATION_JSON).content("{\"role\":\"PARTNER_ADMIN\"}"))
            .andExpect(status().isNoContent());
        verify(managementUseCase).changeRole(new ManagementActor(1L, null, ShopRole.SUPER_ADMIN), 9L,
            ShopRole.PARTNER_ADMIN);
    }

    @Test
    void 브랜드_배정은_브랜드_목록이_필수이고_빈_목록은_허용된다() throws Exception {
        mvc.perform(put("/api/members/9/brands").header("Authorization", partnerAdmin())
                .contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isBadRequest());
        mvc.perform(put("/api/members/9/brands").header("Authorization", partnerAdmin())
                .contentType(MediaType.APPLICATION_JSON).content("{\"brandIds\":[]}"))
            .andExpect(status().isNoContent());
        verify(managementUseCase).assignBrands(new ManagementActor(5L, 7L, ShopRole.PARTNER_ADMIN), 9L, Set.of());
    }

    @Test
    void 멤버_제거는_행위자와_함께_전달된다() throws Exception {
        mvc.perform(delete("/api/members/9").header("Authorization", partnerAdmin()))
            .andExpect(status().isNoContent());
        verify(managementUseCase).remove(new ManagementActor(5L, 7L, ShopRole.PARTNER_ADMIN), 9L);
    }

    @Test
    void 브랜드_생성은_201과_본문을_돌려주고_행위자와_회사_ID를_전달한다() throws Exception {
        when(brandUseCase.create(any(), any(), anyString())).thenReturn(ShopBrand.of(30L, 7L, "새 브랜드", null));

        mvc.perform(post("/api/brands").header("Authorization", partnerAdmin())
                .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"새 브랜드\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(30))
            .andExpect(jsonPath("$.companyId").value(7))
            .andExpect(jsonPath("$.name").value("새 브랜드"));
        mvc.perform(post("/api/brands").header("Authorization", superAdmin())
                .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"새 브랜드\",\"companyId\":8}"));

        verify(brandUseCase).create(new ManagementActor(5L, 7L, ShopRole.PARTNER_ADMIN), null, "새 브랜드");
        verify(brandUseCase).create(new ManagementActor(1L, null, ShopRole.SUPER_ADMIN), 8L, "새 브랜드");
    }

    @Test
    void 브랜드_이름이_비었거나_100자를_넘으면_400이고_서비스를_호출하지_않는다() throws Exception {
        mvc.perform(post("/api/brands").header("Authorization", partnerAdmin())
                .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\" \"}"))
            .andExpect(status().isBadRequest());
        mvc.perform(post("/api/brands").header("Authorization", partnerAdmin())
                .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"" + "가".repeat(101) + "\"}"))
            .andExpect(status().isBadRequest());
        verify(brandUseCase, never()).create(any(), any(), anyString());
    }

    @Test
    void 브랜드_이름_변경과_삭제가_전달된다() throws Exception {
        when(brandUseCase.rename(any(), anyLong(), anyString())).thenReturn(ShopBrand.of(30L, 7L, "변경", null));

        mvc.perform(patch("/api/brands/30").header("Authorization", partnerAdmin())
                .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"변경\"}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.name").value("변경"));
        mvc.perform(delete("/api/brands/30").header("Authorization", partnerAdmin()))
            .andExpect(status().isNoContent());

        ManagementActor actor = new ManagementActor(5L, 7L, ShopRole.PARTNER_ADMIN);
        verify(brandUseCase).rename(actor, 30L, "변경");
        verify(brandUseCase).delete(actor, 30L);
    }

    @Test
    void 접근_신청_본문의_요청_역할이_전달되고_본문이_없어도_된다() throws Exception {
        mvc.perform(post("/api/access-requests").header("Authorization", partnerAdmin())
                .contentType(MediaType.APPLICATION_JSON).content("{\"requestedRole\":\"BRAND_ADMIN\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.requestType").value("COMPANY"))
            .andExpect(jsonPath("$.requestedRole").value("PARTNER_ADMIN"));
        mvc.perform(post("/api/access-requests").header("Authorization", partnerAdmin()))
            .andExpect(status().isOk());
    }

    private static final String CREATE_BODY = "{\"loginId\":\"brand01\",\"name\":\"홍길동\",\"phone\":\"010-1234-5678\","
        + "\"role\":\"BRAND_ADMIN\",\"brandIds\":[3]}";

    @Test
    void 비밀번호_최초_설정은_토큰없이_가능하고_204() throws Exception {
        mvc.perform(post("/api/auth/password-setup").contentType(MediaType.APPLICATION_JSON)
                .content("{\"loginId\":\"brand01\",\"phone\":\"010-1234-5678\",\"newPassword\":\"Passw0rd!\"}"))
            .andExpect(status().isNoContent());
        verify(passwordSetupUseCase).setup("brand01", "010-1234-5678", "Passw0rd!", "127.0.0.1");
    }

    @Test
    void 비밀번호_설정_입력이_규칙에_맞지_않으면_400이고_서비스를_호출하지_않는다() throws Exception {
        mvc.perform(post("/api/auth/password-setup").contentType(MediaType.APPLICATION_JSON)
                .content("{\"loginId\":\"brand01\",\"phone\":\"010-1234-5678\",\"newPassword\":\"short\"}"))
            .andExpect(status().isBadRequest());
        verify(passwordSetupUseCase, never()).setup(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void 관리자_계정_생성은_FULL_토큰이_필요하고_행위자와_함께_전달된다() throws Exception {
        when(managementUseCase.create(any(), any())).thenReturn(88L);

        mvc.perform(post("/api/members").contentType(MediaType.APPLICATION_JSON).content(CREATE_BODY))
            .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/members").header("Authorization", limited())
                .contentType(MediaType.APPLICATION_JSON).content(CREATE_BODY))
            .andExpect(status().isForbidden());
        verifyNoInteractions(managementUseCase);

        mvc.perform(post("/api/members").header("Authorization", partnerAdmin())
                .contentType(MediaType.APPLICATION_JSON).content(CREATE_BODY))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(88));
        verify(managementUseCase).create(new ManagementActor(5L, 7L, ShopRole.PARTNER_ADMIN),
            new CreateMemberCommand("brand01", "홍길동", "010-1234-5678", null, ShopRole.BRAND_ADMIN, null, Set.of(3L)));
    }

    @Test
    void 관리자_계정_생성_입력이_규칙에_맞지_않으면_400이다() throws Exception {
        mvc.perform(post("/api/members").header("Authorization", partnerAdmin())
                .contentType(MediaType.APPLICATION_JSON).content(CREATE_BODY.replace("brand01", "a b")))
            .andExpect(status().isBadRequest());
        mvc.perform(post("/api/members").header("Authorization", partnerAdmin())
                .contentType(MediaType.APPLICATION_JSON).content(CREATE_BODY.replace("010-1234-5678", "abc")))
            .andExpect(status().isBadRequest());
        mvc.perform(post("/api/members").header("Authorization", partnerAdmin())
                .contentType(MediaType.APPLICATION_JSON).content(CREATE_BODY.replace("\"role\":\"BRAND_ADMIN\",", "")))
            .andExpect(status().isBadRequest());
        verifyNoInteractions(managementUseCase);
    }

    @Test
    void 브랜드_관리자의_계정_생성은_서비스가_거부한_403이_내려간다() throws Exception {
        when(managementUseCase.create(any(), any()))
            .thenThrow(new ShopBusinessException(ShopErrorCode.ACCESS_MANAGE_FORBIDDEN));

        mvc.perform(post("/api/members").header("Authorization", brandAdmin())
                .contentType(MediaType.APPLICATION_JSON).content(CREATE_BODY))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.code").value("SHOP_ACCESS_012"));
    }

    @Test
    void 비밀번호_설정_재발급은_FULL_토큰이_필요하고_행위자와_함께_전달된다() throws Exception {
        mvc.perform(post("/api/members/9/password-setup/reissue")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/members/9/password-setup/reissue").header("Authorization", limited()))
            .andExpect(status().isForbidden());
        verifyNoInteractions(managementUseCase);

        mvc.perform(post("/api/members/9/password-setup/reissue").header("Authorization", partnerAdmin()))
            .andExpect(status().isNoContent());
        verify(managementUseCase).reissuePasswordSetup(new ManagementActor(5L, 7L, ShopRole.PARTNER_ADMIN), 9L);
    }

    private static final String ONURIM_BODY = "{\"desiredType\":\"ORDERING\",\"companyPhone\":\"02-111-2222\","
        + "\"address\":\"서울시 강남구\"}";

    private static org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder onurimApply(
            String authorization, String json, org.springframework.mock.web.MockMultipartFile... files) {
        var builder = multipart("/api/onurim-application").header("Authorization", authorization)
            .file(new org.springframework.mock.web.MockMultipartFile(
                "data", "", MediaType.APPLICATION_JSON_VALUE, json.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        for (var file : files) {
            builder.file(file);
        }
        return builder;
    }

    @Test
    void 온우림_신청_API는_토큰이_없으면_401_LIMITED는_403() throws Exception {
        mvc.perform(get("/api/onurim-application")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/onurim-application").header("Authorization", limited())).andExpect(status().isForbidden());
        mvc.perform(onurimApply(limited(), ONURIM_BODY))
            .andExpect(status().isForbidden());
        verifyNoInteractions(onurimUseCase);
    }

    @Test
    void 온우림_신청은_토큰으로_만든_행위자와_함께_전달된다() throws Exception {
        ManagementActor actor = new ManagementActor(5L, 7L, ShopRole.PARTNER_ADMIN);
        when(onurimUseCase.apply(any(), any())).thenReturn(new OnurimApplicationView(
            3L, OnurimApplicationStatus.PENDING, OnurimCompanyType.ORDERING, null, null, null, null));

        mvc.perform(onurimApply(partnerAdmin(), ONURIM_BODY))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value("PENDING"))
            .andExpect(jsonPath("$.desiredType").value("ORDERING"));

        verify(onurimUseCase).apply(org.mockito.ArgumentMatchers.eq(actor), any());
    }

    @Test
    void 온우림_신청의_서류_파트는_명령으로_변환되어_전달된다() throws Exception {
        when(onurimUseCase.apply(any(), any())).thenReturn(new OnurimApplicationView(
            3L, OnurimApplicationStatus.PENDING, OnurimCompanyType.ORDERING, null, null, null, null));
        var biz = new org.springframework.mock.web.MockMultipartFile("businessDoc", "biz.png", "image/png", new byte[]{1, 2});
        var empty = new org.springframework.mock.web.MockMultipartFile("bankDoc", "", "application/octet-stream", new byte[0]);

        mvc.perform(onurimApply(partnerAdmin(), ONURIM_BODY, biz, empty)).andExpect(status().isCreated());

        var captor = org.mockito.ArgumentCaptor.forClass(com.greenlink.shopcore.application.onurim.dto.OnurimApplyCommand.class);
        verify(onurimUseCase).apply(any(), captor.capture());
        org.assertj.core.api.Assertions.assertThat(captor.getValue().businessDoc().fileName()).isEqualTo("biz.png");
        org.assertj.core.api.Assertions.assertThat(captor.getValue().businessDoc().contentType()).isEqualTo("image/png");
        org.assertj.core.api.Assertions.assertThat(captor.getValue().businessDoc().content()).containsExactly(1, 2);
        org.assertj.core.api.Assertions.assertThat(captor.getValue().bankDoc()).isNull();
    }

    @Test
    void 온우림_신청은_data_파트가_없으면_400() throws Exception {
        mvc.perform(multipart("/api/onurim-application").header("Authorization", partnerAdmin())
                .file(new org.springframework.mock.web.MockMultipartFile("businessDoc", "biz.png", "image/png", new byte[]{1})))
            .andExpect(status().isBadRequest());
        verify(onurimUseCase, never()).apply(any(), any());
    }

    @Test
    void 서류_필요_여부_API는_토큰이_필요하고_결과를_돌려준다() throws Exception {
        mvc.perform(get("/api/onurim-application/documents")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/onurim-application/documents").header("Authorization", limited()))
            .andExpect(status().isForbidden());

        when(onurimUseCase.getDocumentRequirement(any()))
            .thenReturn(new com.greenlink.shopcore.application.onurim.dto.OnurimDocumentRequirement(true, false));
        mvc.perform(get("/api/onurim-application/documents").header("Authorization", partnerAdmin()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.needsBusinessReg").value(true))
            .andExpect(jsonPath("$.needsBankBook").value(false));
    }

    @Test
    void 온우림_신청_입력이_규칙에_맞지_않으면_400이고_서비스를_호출하지_않는다() throws Exception {
        mvc.perform(onurimApply(partnerAdmin(), ONURIM_BODY.replace("02-111-2222", " ")))
            .andExpect(status().isBadRequest());
        mvc.perform(onurimApply(partnerAdmin(), ONURIM_BODY.replace("ORDERING", "UNKNOWN")))
            .andExpect(status().isBadRequest());
        mvc.perform(onurimApply(partnerAdmin(), ONURIM_BODY.replace("}", ",\"regions\":[{\"sido\":\"서울,경기\"}]}")))
            .andExpect(status().isBadRequest());
        mvc.perform(onurimApply(partnerAdmin(), ONURIM_BODY.replace("}", ",\"taxInvoiceEmail\":\"not-email\"}")))
            .andExpect(status().isBadRequest());
        verify(onurimUseCase, never()).apply(any(), any());
    }

    @Test
    void 브랜드_관리자는_서비스가_403으로_거부한다() throws Exception {
        when(onurimUseCase.getLatest(any())).thenThrow(new ShopBusinessException(ShopErrorCode.ACCESS_MANAGE_FORBIDDEN));

        mvc.perform(get("/api/onurim-application").header("Authorization", brandAdmin()))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.code").value("SHOP_ACCESS_012"));
    }

    @Test
    void 온우림_신청_이력이_없으면_204() throws Exception {
        when(onurimUseCase.getLatest(any())).thenReturn(java.util.Optional.empty());

        mvc.perform(get("/api/onurim-application").header("Authorization", partnerAdmin()))
            .andExpect(status().isNoContent());
    }
}
