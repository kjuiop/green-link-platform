package com.greenlink.shopcore.application.auth;

import com.greenlink.shopcore.application.auth.dto.ShopSession;
import com.greenlink.shopcore.application.auth.dto.VerifiedIdentity;
import com.greenlink.shopcore.application.auth.port.AccessTokenIssuer;
import com.greenlink.shopcore.application.auth.service.ShopSessionResolver;
import com.greenlink.shopcore.domain.auth.AccessStatus;
import com.greenlink.shopcore.domain.auth.ShopRole;
import com.greenlink.shopcore.domain.auth.TokenScope;
import com.greenlink.shopcore.domain.member.model.ShopAdminMember;
import com.greenlink.shopcore.domain.member.model.ShopAdminStatus;
import com.greenlink.shopcore.domain.member.port.ShopAdminMemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ShopSessionResolverTest {

    @Mock ShopAdminMemberRepository memberRepository;
    @Mock AccessTokenIssuer tokenIssuer;

    private ShopSessionResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new ShopSessionResolver(memberRepository, tokenIssuer);
    }

    private VerifiedIdentity identity(Long companyId, boolean superAdmin) {
        return new VerifiedIdentity(10L, "login", "이름", companyId, companyId == null ? null : "회사",
            true, false, true, superAdmin);
    }

    private ShopAdminMember member(ShopAdminStatus status, ShopRole role, Long companyId) {
        return ShopAdminMember.of(1L, 10L, companyId, role, ShopRole.PARTNER_ADMIN, status,
            LocalDateTime.of(2026, 11, 23, 10, 0), null, null, status == ShopAdminStatus.REJECTED ? "사유" : null);
    }

    @Test
    void 슈퍼관리자는_row_없이_FULL_토큰을_받는다() {
        given(tokenIssuer.issue(10L, null, ShopRole.SUPER_ADMIN, TokenScope.FULL)).willReturn("tok");

        ShopSession s = resolver.resolve(identity(null, true));

        assertThat(s.accessToken()).isEqualTo("tok");
        assertThat(s.role()).isEqualTo(ShopRole.SUPER_ADMIN);
        assertThat(s.accessStatus()).isEqualTo(AccessStatus.APPROVED);
        verify(memberRepository, never()).findByAdminId(any());
    }

    @Test
    void 승인된_파트너_관리자는_회사_ID가_담긴_FULL_토큰을_받는다() {
        given(memberRepository.findByAdminId(10L))
            .willReturn(Optional.of(member(ShopAdminStatus.APPROVED, ShopRole.PARTNER_ADMIN, 7L)));
        given(tokenIssuer.issue(10L, 7L, ShopRole.PARTNER_ADMIN, TokenScope.FULL)).willReturn("tok");

        ShopSession s = resolver.resolve(identity(7L, false));

        assertThat(s.role()).isEqualTo(ShopRole.PARTNER_ADMIN);
        assertThat(s.companyId()).isEqualTo(7L);
        assertThat(s.companyName()).isEqualTo("회사");
        assertThat(s.accessStatus()).isEqualTo(AccessStatus.APPROVED);
    }

    @Test
    void 승인된_브랜드_관리자는_멤버에_저장된_역할로_토큰을_받는다() {
        given(memberRepository.findByAdminId(10L))
            .willReturn(Optional.of(member(ShopAdminStatus.APPROVED, ShopRole.BRAND_ADMIN, 7L)));
        given(tokenIssuer.issue(10L, 7L, ShopRole.BRAND_ADMIN, TokenScope.FULL)).willReturn("tok");

        assertThat(resolver.resolve(identity(7L, false)).role()).isEqualTo(ShopRole.BRAND_ADMIN);
    }

    @Test
    void row가_없으면_NONE이고_역할_없는_LIMITED_토큰이다() {
        given(memberRepository.findByAdminId(10L)).willReturn(Optional.empty());
        given(tokenIssuer.issue(10L, 7L, null, TokenScope.LIMITED)).willReturn("tok");

        ShopSession s = resolver.resolve(identity(7L, false));

        assertThat(s.accessStatus()).isEqualTo(AccessStatus.NONE);
        assertThat(s.role()).isNull();
        assertThat(s.companyId()).isEqualTo(7L);
    }

    @Test
    void 대기와_거절_계정은_LIMITED_토큰을_받는다() {
        given(memberRepository.findByAdminId(10L)).willReturn(
            Optional.of(member(ShopAdminStatus.PENDING, null, 7L)),
            Optional.of(member(ShopAdminStatus.REJECTED, null, 7L)));
        given(tokenIssuer.issue(10L, 7L, null, TokenScope.LIMITED)).willReturn("tok");

        assertThat(resolver.resolve(identity(7L, false)).accessStatus()).isEqualTo(AccessStatus.PENDING);
        assertThat(resolver.resolve(identity(7L, false)).accessStatus()).isEqualTo(AccessStatus.REJECTED);
    }

    @Test
    void 온우림의_회사가_멤버의_회사와_다르면_승인되어_있어도_FULL_토큰을_주지_않는다() {
        given(memberRepository.findByAdminId(10L))
            .willReturn(Optional.of(member(ShopAdminStatus.APPROVED, ShopRole.PARTNER_ADMIN, 7L)));
        given(tokenIssuer.issue(10L, 8L, null, TokenScope.LIMITED)).willReturn("tok");

        ShopSession s = resolver.resolve(identity(8L, false));

        assertThat(s.accessStatus()).isEqualTo(AccessStatus.NONE);
        assertThat(s.role()).isNull();
    }

    @Test
    void 로그인은_읽기_전용이라_멤버를_저장하지_않는다() {
        given(memberRepository.findByAdminId(10L))
            .willReturn(Optional.of(member(ShopAdminStatus.APPROVED, ShopRole.PARTNER_ADMIN, 7L)));
        given(tokenIssuer.issue(10L, 7L, ShopRole.PARTNER_ADMIN, TokenScope.FULL)).willReturn("tok");

        resolver.resolve(identity(7L, false));

        verify(memberRepository, never()).save(any());
    }
}
