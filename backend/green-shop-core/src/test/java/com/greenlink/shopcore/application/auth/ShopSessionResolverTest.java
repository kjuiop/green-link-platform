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

    private VerifiedIdentity identity(boolean superAdmin) {
        return new VerifiedIdentity(10L, "newLogin", "새이름", "새회사", superAdmin);
    }

    private ShopAdminMember member(ShopAdminStatus status) {
        return ShopAdminMember.of(1L, 10L, "oldLogin", "옛이름", "옛회사", status,
            LocalDateTime.of(2026, 11, 23, 10, 0), null, null, status == ShopAdminStatus.REJECTED ? "사유" : null);
    }

    @Test
    void 슈퍼관리자는_row_없이_FULL_토큰을_받는다() {
        given(tokenIssuer.issue(10L, ShopRole.SUPER_ADMIN, TokenScope.FULL)).willReturn("tok");

        ShopSession s = resolver.resolve(identity(true));

        assertThat(s.accessToken()).isEqualTo("tok");
        assertThat(s.role()).isEqualTo(ShopRole.SUPER_ADMIN);
        assertThat(s.accessStatus()).isEqualTo(AccessStatus.APPROVED);
        verify(memberRepository, never()).findByOnurimAdminId(any());
        verify(memberRepository, never()).save(any());
    }

    @Test
    void 승인된_계정은_FULL_토큰을_받는다() {
        given(memberRepository.findByOnurimAdminId(10L)).willReturn(Optional.of(member(ShopAdminStatus.APPROVED)));
        given(tokenIssuer.issue(10L, ShopRole.SHOP_ADMIN, TokenScope.FULL)).willReturn("tok");

        ShopSession s = resolver.resolve(identity(false));

        assertThat(s.role()).isEqualTo(ShopRole.SHOP_ADMIN);
        assertThat(s.accessStatus()).isEqualTo(AccessStatus.APPROVED);
    }

    @Test
    void row가_없으면_NONE이고_LIMITED_토큰이다() {
        given(memberRepository.findByOnurimAdminId(10L)).willReturn(Optional.empty());
        given(tokenIssuer.issue(10L, null, TokenScope.LIMITED)).willReturn("tok");

        ShopSession s = resolver.resolve(identity(false));

        assertThat(s.accessStatus()).isEqualTo(AccessStatus.NONE);
        assertThat(s.role()).isEqualTo(ShopRole.SHOP_ADMIN);
        verify(memberRepository, never()).save(any());
    }

    @Test
    void 대기와_거절_계정은_LIMITED_토큰을_받는다() {
        given(memberRepository.findByOnurimAdminId(10L))
            .willReturn(Optional.of(member(ShopAdminStatus.PENDING)), Optional.of(member(ShopAdminStatus.REJECTED)));
        given(tokenIssuer.issue(10L, null, TokenScope.LIMITED)).willReturn("tok");

        assertThat(resolver.resolve(identity(false)).accessStatus()).isEqualTo(AccessStatus.PENDING);
        assertThat(resolver.resolve(identity(false)).accessStatus()).isEqualTo(AccessStatus.REJECTED);
    }

    @Test
    void 로그인할_때마다_이름_loginId_회사명_스냅샷을_갱신해_저장한다() {
        ShopAdminMember m = member(ShopAdminStatus.APPROVED);
        given(memberRepository.findByOnurimAdminId(10L)).willReturn(Optional.of(m));
        given(tokenIssuer.issue(10L, ShopRole.SHOP_ADMIN, TokenScope.FULL)).willReturn("tok");

        resolver.resolve(identity(false));

        assertThat(m.getLoginId()).isEqualTo("newLogin");
        assertThat(m.getName()).isEqualTo("새이름");
        assertThat(m.getCompanyName()).isEqualTo("새회사");
        verify(memberRepository).save(m);
    }
}
