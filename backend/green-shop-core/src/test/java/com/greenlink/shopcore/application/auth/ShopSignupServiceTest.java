package com.greenlink.shopcore.application.auth;

import com.greenlink.shopcore.application.auth.dto.RegisteredAccount;
import com.greenlink.shopcore.application.auth.dto.SignupCommand;
import com.greenlink.shopcore.application.auth.dto.SignupResult;
import com.greenlink.shopcore.application.auth.dto.VerifiedIdentity;
import com.greenlink.shopcore.application.auth.port.IdentityRegistrar;
import com.greenlink.shopcore.application.auth.port.SignupRateLimiter;
import com.greenlink.shopcore.application.auth.port.SignupRateLimiter.Bucket;
import com.greenlink.shopcore.application.auth.service.ShopSignupService;
import com.greenlink.shopcore.domain.auth.ShopRole;
import com.greenlink.shopcore.domain.member.model.ShopAdminMember;
import com.greenlink.shopcore.domain.member.model.ShopAdminStatus;
import com.greenlink.shopcore.domain.member.port.ShopAdminMemberRepository;
import com.greenlink.shopcore.global.exception.ErrorCode;
import com.greenlink.shopcore.global.exception.ShopBusinessException;
import com.greenlink.shopcore.global.exception.ShopErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class ShopSignupServiceTest {

    private static final String IP = "1.2.3.4";

    @Mock IdentityRegistrar registrar;
    @Mock ShopAdminMemberRepository memberRepository;
    @Mock SignupRateLimiter rateLimiter;

    private ShopSignupService service;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-11-25T01:00:00Z"), ZoneId.of("Asia/Seoul"));
        service = new ShopSignupService(registrar, memberRepository, rateLimiter, clock);
    }

    private SignupCommand cmd(String requestedRole) {
        return new SignupCommand("shop01", "Passw0rd!", "홍길동", "010-1234-5678", null,
            "123-45-67890", "꽃집", "김대표", requestedRole);
    }

    private VerifiedIdentity identity(Long companyId) {
        return new VerifiedIdentity(10L, "shop01", "홍길동", companyId, companyId == null ? null : "꽃집",
            false, true, companyId != null, false);
    }

    private void assertCode(Runnable call, ErrorCode expected) {
        assertThatThrownBy(call::run).isInstanceOfSatisfying(ShopBusinessException.class,
            e -> assertThat(e.getErrorCode()).isEqualTo(expected));
    }

    @Test
    void 창립자는_승인된_PARTNER_ADMIN_멤버가_저장된다() {
        given(rateLimiter.tryAcquire(Bucket.SIGNUP, IP)).willReturn(true);
        given(registrar.register(any())).willReturn(new RegisteredAccount(identity(20L), 20L, true));
        given(memberRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        SignupResult r = service.signup(cmd(null), IP);

        assertThat(r.adminId()).isEqualTo(10L);
        assertThat(r.founder()).isTrue();
        ArgumentCaptor<ShopAdminMember> captor = ArgumentCaptor.forClass(ShopAdminMember.class);
        verify(memberRepository).save(captor.capture());
        ShopAdminMember saved = captor.getValue();
        assertThat(saved.getStatus()).isEqualTo(ShopAdminStatus.APPROVED);
        assertThat(saved.getRole()).isEqualTo(ShopRole.PARTNER_ADMIN);
        assertThat(saved.getCompanyId()).isEqualTo(20L);
        assertThat(saved.getAdminId()).isEqualTo(10L);
    }

    @Test
    void 합류_신청자는_대상_회사와_희망_역할로_PENDING_멤버가_저장된다() {
        given(rateLimiter.tryAcquire(Bucket.SIGNUP, IP)).willReturn(true);
        given(registrar.register(any())).willReturn(new RegisteredAccount(identity(null), 7L, false));
        given(memberRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        SignupResult r = service.signup(cmd("BRAND_ADMIN"), IP);

        assertThat(r.founder()).isFalse();
        ArgumentCaptor<ShopAdminMember> captor = ArgumentCaptor.forClass(ShopAdminMember.class);
        verify(memberRepository).save(captor.capture());
        ShopAdminMember saved = captor.getValue();
        assertThat(saved.getStatus()).isEqualTo(ShopAdminStatus.PENDING);
        assertThat(saved.getCompanyId()).isEqualTo(7L);
        assertThat(saved.getRequestedRole()).isEqualTo(ShopRole.BRAND_ADMIN);
        assertThat(saved.getRole()).isNull();
    }

    @Test
    void 한도를_넘으면_429이고_온우림을_호출하지_않는다() {
        given(rateLimiter.tryAcquire(Bucket.SIGNUP, IP)).willReturn(false);

        assertCode(() -> service.signup(cmd(null), IP), ShopErrorCode.SIGNUP_RATE_LIMITED);
        verifyNoInteractions(registrar, memberRepository);
    }

    @Test
    void 알_수_없거나_슈퍼관리자_역할이면_온우림을_호출하기_전에_거부한다() {
        given(rateLimiter.tryAcquire(Bucket.SIGNUP, IP)).willReturn(true);

        assertCode(() -> service.signup(cmd("HACKER"), IP), ShopErrorCode.SIGNUP_INVALID_INPUT);
        assertCode(() -> service.signup(cmd("SUPER_ADMIN"), IP), ShopErrorCode.SIGNUP_INVALID_INPUT);
        verifyNoInteractions(registrar, memberRepository);
    }

    @Test
    void 온우림_거부는_멤버를_저장하지_않고_그대로_전파한다() {
        given(rateLimiter.tryAcquire(Bucket.SIGNUP, IP)).willReturn(true);
        given(registrar.register(any()))
            .willThrow(new ShopBusinessException(ShopErrorCode.SIGNUP_LOGIN_ID_DUPLICATED));

        assertCode(() -> service.signup(cmd(null), IP), ShopErrorCode.SIGNUP_LOGIN_ID_DUPLICATED);
        verify(memberRepository, never()).save(any());
    }

    @Test
    void 멤버_저장에_실패하면_예외를_전파한다() {
        given(rateLimiter.tryAcquire(Bucket.SIGNUP, IP)).willReturn(true);
        given(registrar.register(any())).willReturn(new RegisteredAccount(identity(20L), 20L, true));
        given(memberRepository.save(any())).willThrow(new IllegalStateException("db down"));

        assertThatThrownBy(() -> service.signup(cmd(null), IP)).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void 사업자번호_조회는_조회_버킷을_쓰고_결과를_반환한다() {
        given(rateLimiter.tryAcquire(Bucket.LOOKUP, IP)).willReturn(true);
        given(registrar.businessNoExists("123-45-67890")).willReturn(true);

        assertThat(service.businessNoExists("123-45-67890", IP)).isTrue();
    }

    @Test
    void 사업자번호_조회도_한도를_넘으면_429() {
        given(rateLimiter.tryAcquire(Bucket.LOOKUP, IP)).willReturn(false);

        assertCode(() -> service.businessNoExists("123-45-67890", IP), ShopErrorCode.SIGNUP_RATE_LIMITED);
        verifyNoInteractions(registrar);
    }
}
