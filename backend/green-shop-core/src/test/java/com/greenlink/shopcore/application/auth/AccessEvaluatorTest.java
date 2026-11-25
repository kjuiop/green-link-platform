package com.greenlink.shopcore.application.auth;

import com.greenlink.shopcore.application.auth.dto.AccessDecision;
import com.greenlink.shopcore.application.auth.dto.VerifiedIdentity;
import com.greenlink.shopcore.application.auth.service.AccessEvaluator;
import com.greenlink.shopcore.domain.auth.AccessRequestType;
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
class AccessEvaluatorTest {

    @Mock ShopAdminMemberRepository memberRepository;

    private AccessEvaluator evaluator;

    @BeforeEach
    void setUp() {
        evaluator = new AccessEvaluator(memberRepository);
    }

    private VerifiedIdentity identity(Long companyId, boolean siteEnabled, boolean superAdmin) {
        return new VerifiedIdentity(10L, "login", "이름", companyId, companyId == null ? null : "회사",
            true, siteEnabled, true, superAdmin);
    }

    private ShopAdminMember member(ShopAdminStatus status, ShopRole role, ShopRole requested, Long companyId) {
        return ShopAdminMember.of(1L, 10L, companyId, role, requested, status,
            LocalDateTime.of(2026, 11, 23, 10, 0), null, null, status == ShopAdminStatus.REJECTED ? "사유" : null);
    }

    @Test
    void 슈퍼관리자는_멤버_조회_없이_FULL이다() {
        AccessDecision d = evaluator.evaluate(identity(null, false, true));

        assertThat(d.status()).isEqualTo(AccessStatus.APPROVED);
        assertThat(d.role()).isEqualTo(ShopRole.SUPER_ADMIN);
        assertThat(d.scope()).isEqualTo(TokenScope.FULL);
        verify(memberRepository, never()).findByAdminId(any());
    }

    @Test
    void 멤버가_없으면_NONE_LIMITED이고_요청_유형을_함께_돌려준다() {
        given(memberRepository.findByAdminId(10L)).willReturn(Optional.empty());

        AccessDecision d = evaluator.evaluate(identity(7L, false, false));

        assertThat(d.status()).isEqualTo(AccessStatus.NONE);
        assertThat(d.scope()).isEqualTo(TokenScope.LIMITED);
        assertThat(d.requestType()).isEqualTo(AccessRequestType.COMPANY);
    }

    @Test
    void 쇼핑몰이_켜진_회사나_소속없는_계정의_요청_유형은_JOIN이다() {
        given(memberRepository.findByAdminId(10L)).willReturn(Optional.empty());

        assertThat(evaluator.evaluate(identity(7L, true, false)).requestType()).isEqualTo(AccessRequestType.JOIN);
        assertThat(evaluator.evaluate(identity(null, false, false)).requestType()).isEqualTo(AccessRequestType.JOIN);
    }

    @Test
    void 승인된_멤버는_회사_쇼핑몰이_켜져_있으면_FULL과_저장된_역할이다() {
        given(memberRepository.findByAdminId(10L)).willReturn(
            Optional.of(member(ShopAdminStatus.APPROVED, ShopRole.BRAND_ADMIN, ShopRole.PARTNER_ADMIN, 7L)));

        AccessDecision d = evaluator.evaluate(identity(7L, true, false));

        assertThat(d.status()).isEqualTo(AccessStatus.APPROVED);
        assertThat(d.role()).isEqualTo(ShopRole.BRAND_ADMIN);
        assertThat(d.scope()).isEqualTo(TokenScope.FULL);
        assertThat(d.requestedRole()).isEqualTo(ShopRole.PARTNER_ADMIN);
    }

    @Test
    void 승인된_멤버라도_회사_쇼핑몰이_꺼져_있으면_SITE_DISABLED_LIMITED이다() {
        given(memberRepository.findByAdminId(10L)).willReturn(
            Optional.of(member(ShopAdminStatus.APPROVED, ShopRole.PARTNER_ADMIN, ShopRole.PARTNER_ADMIN, 7L)));

        AccessDecision d = evaluator.evaluate(identity(7L, false, false));

        assertThat(d.status()).isEqualTo(AccessStatus.SITE_DISABLED);
        assertThat(d.role()).isNull();
        assertThat(d.scope()).isEqualTo(TokenScope.LIMITED);
    }

    @Test
    void 승인된_멤버의_회사가_다르면_NONE이다() {
        given(memberRepository.findByAdminId(10L)).willReturn(
            Optional.of(member(ShopAdminStatus.APPROVED, ShopRole.PARTNER_ADMIN, ShopRole.PARTNER_ADMIN, 7L)));

        assertThat(evaluator.evaluate(identity(8L, true, false)).status()).isEqualTo(AccessStatus.NONE);
        assertThat(evaluator.evaluate(identity(null, true, false)).status()).isEqualTo(AccessStatus.NONE);
    }

    @Test
    void 대기_거절은_회사_필터_없이_상태_사유_요청역할을_돌려준다() {
        given(memberRepository.findByAdminId(10L)).willReturn(
            Optional.of(member(ShopAdminStatus.PENDING, null, ShopRole.BRAND_ADMIN, 7L)),
            Optional.of(member(ShopAdminStatus.REJECTED, null, ShopRole.BRAND_ADMIN, 7L)));

        AccessDecision pending = evaluator.evaluate(identity(null, false, false));
        AccessDecision rejected = evaluator.evaluate(identity(null, false, false));

        assertThat(pending.status()).isEqualTo(AccessStatus.PENDING);
        assertThat(pending.requestedRole()).isEqualTo(ShopRole.BRAND_ADMIN);
        assertThat(pending.scope()).isEqualTo(TokenScope.LIMITED);
        assertThat(rejected.status()).isEqualTo(AccessStatus.REJECTED);
        assertThat(rejected.rejectReason()).isEqualTo("사유");
    }
}
