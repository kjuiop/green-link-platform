package com.greenlink.shopcore.application.admin;

import com.greenlink.shopcore.application.access.port.CompanyAccessGranter;
import com.greenlink.shopcore.application.admin.dto.ShopAdminView;
import com.greenlink.shopcore.application.admin.service.ShopAdminManagementService;
import com.greenlink.shopcore.application.auth.dto.AccountProfile;
import com.greenlink.shopcore.application.auth.port.IdentityVerifier;
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
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class ShopAdminManagementServiceTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-11-23T01:00:00Z"), ZoneId.of("Asia/Seoul"));
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 23, 10, 0);

    @Mock ShopAdminMemberRepository memberRepository;
    @Mock IdentityVerifier identityVerifier;
    @Mock CompanyAccessGranter accessGranter;

    private ShopAdminManagementService service;

    @BeforeEach
    void setUp() {
        service = new ShopAdminManagementService(memberRepository, identityVerifier, accessGranter, CLOCK);
    }

    private ShopAdminMember member(ShopAdminStatus status) {
        return ShopAdminMember.of(1L, 10L, 7L, status == ShopAdminStatus.APPROVED ? ShopRole.PARTNER_ADMIN : null,
            ShopRole.PARTNER_ADMIN, status, NOW.minusDays(1), null, null, null);
    }

    private void assertCode(Runnable call, ErrorCode expected) {
        assertThatThrownBy(call::run).isInstanceOfSatisfying(ShopBusinessException.class,
            e -> assertThat(e.getErrorCode()).isEqualTo(expected));
    }

    @Test
    void 목록은_상태_필터를_저장소에_전달하고_온우림_배치_조회로_이름을_채운다() {
        given(memberRepository.findAll(ShopAdminStatus.PENDING)).willReturn(List.of(member(ShopAdminStatus.PENDING)));
        given(identityVerifier.getAccounts(List.of(10L)))
            .willReturn(List.of(new AccountProfile(10L, "login", "이름", "회사")));

        List<ShopAdminView> result = service.list(ShopAdminStatus.PENDING);

        assertThat(result).hasSize(1);
        ShopAdminView v = result.get(0);
        assertThat(v.adminId()).isEqualTo(10L);
        assertThat(v.companyId()).isEqualTo(7L);
        assertThat(v.loginId()).isEqualTo("login");
        assertThat(v.name()).isEqualTo("이름");
        assertThat(v.companyName()).isEqualTo("회사");
        assertThat(v.requestedRole()).isEqualTo(ShopRole.PARTNER_ADMIN);
        assertThat(v.role()).isNull();
        assertThat(v.status()).isEqualTo(ShopAdminStatus.PENDING);
    }

    @Test
    void 승인된_멤버는_저장된_역할이_노출된다() {
        given(memberRepository.findAll(null)).willReturn(List.of(member(ShopAdminStatus.APPROVED)));
        given(identityVerifier.getAccounts(List.of(10L))).willReturn(List.of());

        assertThat(service.list(null).get(0).role()).isEqualTo(ShopRole.PARTNER_ADMIN);
    }

    @Test
    void 온우림에_없는_계정은_이름_없이_반환된다() {
        given(memberRepository.findAll(null)).willReturn(List.of(member(ShopAdminStatus.PENDING)));
        given(identityVerifier.getAccounts(List.of(10L))).willReturn(List.of());

        ShopAdminView v = service.list(null).get(0);

        assertThat(v.name()).isNull();
        assertThat(v.loginId()).isNull();
        assertThat(v.companyName()).isNull();
    }

    @Test
    void 온우림_조회가_실패해도_목록은_이름_없이_반환된다() {
        given(memberRepository.findAll(null)).willReturn(List.of(member(ShopAdminStatus.PENDING)));
        given(identityVerifier.getAccounts(any()))
            .willThrow(new ShopBusinessException(ShopErrorCode.AUTH_UPSTREAM_UNAVAILABLE));

        List<ShopAdminView> result = service.list(null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isNull();
        assertThat(result.get(0).status()).isEqualTo(ShopAdminStatus.PENDING);
    }

    @Test
    void 목록이_비어_있으면_온우림을_조회하지_않는다() {
        given(memberRepository.findAll(null)).willReturn(List.of());

        assertThat(service.list(null)).isEmpty();
        verifyNoInteractions(identityVerifier);
    }

    @Test
    void 승인하면_온우림_권한부여_후_처리자와_시각이_기록되어_저장된다() {
        ShopAdminMember m = member(ShopAdminStatus.PENDING);
        given(memberRepository.findById(1L)).willReturn(Optional.of(m));

        service.approve(1L, null, 99L);

        assertThat(m.getStatus()).isEqualTo(ShopAdminStatus.APPROVED);
        assertThat(m.getRole()).isEqualTo(ShopRole.PARTNER_ADMIN);
        assertThat(m.getProcessedBy()).isEqualTo(99L);
        assertThat(m.getProcessedAt()).isEqualTo(NOW);
        InOrder order = inOrder(accessGranter, memberRepository);
        order.verify(accessGranter).grant(10L, 7L);
        order.verify(memberRepository).save(m);
    }

    @Test
    void 신청보다_낮은_역할로_승인할_수_있다() {
        ShopAdminMember m = member(ShopAdminStatus.PENDING);
        given(memberRepository.findById(1L)).willReturn(Optional.of(m));
        given(memberRepository.existsApprovedByCompanyId(7L)).willReturn(true);

        service.approve(1L, ShopRole.BRAND_ADMIN, 99L);

        assertThat(m.getRole()).isEqualTo(ShopRole.BRAND_ADMIN);
        verify(memberRepository).save(m);
    }

    @Test
    void 신청보다_높은_역할로의_승인은_온우림_호출_없이_거부된다() {
        ShopAdminMember m = ShopAdminMember.request(10L, 7L, ShopRole.BRAND_ADMIN, NOW.minusDays(1));
        given(memberRepository.findById(1L)).willReturn(Optional.of(m));

        assertCode(() -> service.approve(1L, ShopRole.PARTNER_ADMIN, 99L), ShopErrorCode.ACCESS_ROLE_EXCEEDS_REQUEST);
        verifyNoInteractions(accessGranter);
        verify(memberRepository, never()).save(any());
    }

    @Test
    void 슈퍼관리자_역할로의_승인은_거부된다() {
        given(memberRepository.findById(1L)).willReturn(Optional.of(member(ShopAdminStatus.PENDING)));

        assertCode(() -> service.approve(1L, ShopRole.SUPER_ADMIN, 99L), ShopErrorCode.ACCESS_ROLE_NOT_ASSIGNABLE);
        verifyNoInteractions(accessGranter);
    }

    @Test
    void 회사의_첫_승인_멤버가_브랜드_관리자면_거부된다() {
        ShopAdminMember m = member(ShopAdminStatus.PENDING);
        given(memberRepository.findById(1L)).willReturn(Optional.of(m));
        given(memberRepository.existsApprovedByCompanyId(7L)).willReturn(false);

        assertCode(() -> service.approve(1L, ShopRole.BRAND_ADMIN, 99L), ShopErrorCode.ACCESS_FIRST_MEMBER_MUST_BE_PARTNER);
        verifyNoInteractions(accessGranter);
        verify(memberRepository, never()).save(any());
    }

    @Test
    void 이미_승인된_멤버가_있는_회사는_브랜드_관리자로_승인할_수_있다() {
        ShopAdminMember m = member(ShopAdminStatus.PENDING);
        given(memberRepository.findById(1L)).willReturn(Optional.of(m));
        given(memberRepository.existsApprovedByCompanyId(7L)).willReturn(true);

        service.approve(1L, ShopRole.BRAND_ADMIN, 99L);

        assertThat(m.getRole()).isEqualTo(ShopRole.BRAND_ADMIN);
    }

    @Test
    void 온우림_권한부여가_실패하면_멤버는_PENDING으로_남고_저장되지_않는다() {
        ShopAdminMember m = member(ShopAdminStatus.PENDING);
        given(memberRepository.findById(1L)).willReturn(Optional.of(m));
        doThrow(new ShopBusinessException(ShopErrorCode.AUTH_UPSTREAM_UNAVAILABLE)).when(accessGranter).grant(10L, 7L);

        assertCode(() -> service.approve(1L, null, 99L), ShopErrorCode.AUTH_UPSTREAM_UNAVAILABLE);

        assertThat(m.getStatus()).isEqualTo(ShopAdminStatus.PENDING);
        assertThat(m.getRole()).isNull();
        verify(memberRepository, never()).save(any());
    }

    @Test
    void 온우림_실패_후_재시도하면_승인된다() {
        ShopAdminMember m = member(ShopAdminStatus.PENDING);
        given(memberRepository.findById(1L)).willReturn(Optional.of(m));
        doThrow(new ShopBusinessException(ShopErrorCode.AUTH_UPSTREAM_UNAVAILABLE))
            .doNothing().when(accessGranter).grant(10L, 7L);

        assertCode(() -> service.approve(1L, null, 99L), ShopErrorCode.AUTH_UPSTREAM_UNAVAILABLE);
        service.approve(1L, null, 99L);

        assertThat(m.getStatus()).isEqualTo(ShopAdminStatus.APPROVED);
        verify(memberRepository).save(m);
    }

    @Test
    void 목록은_소속없는_합류_신청자의_회사명을_회사_배치_조회로_채운다() {
        given(memberRepository.findAll(null)).willReturn(List.of(member(ShopAdminStatus.PENDING)));
        given(identityVerifier.getAccounts(List.of(10L)))
            .willReturn(List.of(new AccountProfile(10L, "login", "이름", null)));
        given(identityVerifier.getCompanyNames(List.of(7L))).willReturn(Map.of(7L, "신청 회사"));

        assertThat(service.list(null).get(0).companyName()).isEqualTo("신청 회사");
    }

    @Test
    void 회사명_조회가_실패해도_목록은_반환된다() {
        given(memberRepository.findAll(null)).willReturn(List.of(member(ShopAdminStatus.PENDING)));
        given(identityVerifier.getAccounts(List.of(10L)))
            .willReturn(List.of(new AccountProfile(10L, "login", "이름", null)));
        given(identityVerifier.getCompanyNames(any()))
            .willThrow(new ShopBusinessException(ShopErrorCode.AUTH_UPSTREAM_UNAVAILABLE));

        ShopAdminView v = service.list(null).get(0);

        assertThat(v.loginId()).isEqualTo("login");
        assertThat(v.companyName()).isNull();
    }

    @Test
    void 거절하면_사유가_기록되어_저장된다() {
        ShopAdminMember m = member(ShopAdminStatus.PENDING);
        given(memberRepository.findById(1L)).willReturn(Optional.of(m));

        service.reject(1L, "서류 미비", 99L);

        assertThat(m.getStatus()).isEqualTo(ShopAdminStatus.REJECTED);
        assertThat(m.getRejectReason()).isEqualTo("서류 미비");
        verify(memberRepository).save(m);
    }

    @Test
    void 사유_없는_거절은_거부되고_저장되지_않는다() {
        given(memberRepository.findById(1L)).willReturn(Optional.of(member(ShopAdminStatus.PENDING)));

        assertCode(() -> service.reject(1L, " ", 99L), ShopErrorCode.ACCESS_REJECT_REASON_REQUIRED);
        verify(memberRepository, never()).save(any());
    }

    @Test
    void 이미_처리된_건은_승인_거절이_거부된다() {
        given(memberRepository.findById(1L))
            .willReturn(Optional.of(member(ShopAdminStatus.APPROVED)), Optional.of(member(ShopAdminStatus.REJECTED)));

        assertCode(() -> service.approve(1L, null, 99L), ShopErrorCode.ACCESS_INVALID_TRANSITION);
        assertCode(() -> service.reject(1L, "사유", 99L), ShopErrorCode.ACCESS_INVALID_TRANSITION);
        verify(memberRepository, never()).save(any());
        verifyNoInteractions(accessGranter);
    }

    @Test
    void 없는_id는_ADMIN_REQUEST_NOT_FOUND() {
        given(memberRepository.findById(5L)).willReturn(Optional.empty());

        assertCode(() -> service.approve(5L, null, 99L), ShopErrorCode.ADMIN_REQUEST_NOT_FOUND);
        assertCode(() -> service.reject(5L, "사유", 99L), ShopErrorCode.ADMIN_REQUEST_NOT_FOUND);
    }
}
