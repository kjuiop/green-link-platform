package com.greenlink.shopcore.application.access;

import com.greenlink.shopcore.application.access.dto.MyAccess;
import com.greenlink.shopcore.application.access.service.AccessRequestService;
import com.greenlink.shopcore.application.auth.dto.VerifiedIdentity;
import com.greenlink.shopcore.application.auth.port.IdentityVerifier;
import com.greenlink.shopcore.domain.auth.AccessStatus;
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
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class AccessRequestServiceTest {

    private static final ZoneId ZONE = ZoneId.of("Asia/Seoul");
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-11-23T01:00:00Z"), ZONE);
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 23, 10, 0);

    @Mock ShopAdminMemberRepository memberRepository;
    @Mock IdentityVerifier identityVerifier;

    private AccessRequestService service;

    @BeforeEach
    void setUp() {
        service = new AccessRequestService(memberRepository, identityVerifier, CLOCK);
    }

    private ShopAdminMember member(ShopAdminStatus status) {
        return ShopAdminMember.of(1L, 10L, 7L, status == ShopAdminStatus.APPROVED ? ShopRole.PARTNER_ADMIN : null,
            ShopRole.PARTNER_ADMIN, status, NOW.minusDays(1), NOW.minusHours(1), 99L,
            status == ShopAdminStatus.REJECTED ? "사유" : null);
    }

    private VerifiedIdentity identity(Long companyId, boolean superAdmin) {
        return new VerifiedIdentity(10L, "login", "이름", companyId, companyId == null ? null : "회사",
            true, false, true, superAdmin);
    }

    private void assertCode(Runnable call, ErrorCode expected) {
        assertThatThrownBy(call::run).isInstanceOfSatisfying(ShopBusinessException.class,
            e -> assertThat(e.getErrorCode()).isEqualTo(expected));
    }

    @Test
    void 내_상태_조회_슈퍼관리자는_APPROVED이고_저장소를_조회하지_않는다() {
        MyAccess r = service.getMyAccess(10L, true);

        assertThat(r.accessStatus()).isEqualTo(AccessStatus.APPROVED);
        verifyNoInteractions(memberRepository);
    }

    @Test
    void 내_상태_조회_row가_없으면_NONE() {
        given(memberRepository.findByAdminId(10L)).willReturn(Optional.empty());

        assertThat(service.getMyAccess(10L, false).accessStatus()).isEqualTo(AccessStatus.NONE);
    }

    @Test
    void 내_상태_조회_거절되면_사유가_함께_반환된다() {
        given(memberRepository.findByAdminId(10L)).willReturn(Optional.of(member(ShopAdminStatus.REJECTED)));

        MyAccess r = service.getMyAccess(10L, false);

        assertThat(r.accessStatus()).isEqualTo(AccessStatus.REJECTED);
        assertThat(r.rejectReason()).isEqualTo("사유");
    }

    @Test
    void 처음_신청하면_회사_ID와_PARTNER_ADMIN_요청_역할로_PENDING_row가_저장된다() {
        given(identityVerifier.getAccount(10L)).willReturn(identity(7L, false));
        given(memberRepository.findByAdminId(10L)).willReturn(Optional.empty());
        given(memberRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        MyAccess r = service.apply(10L, false);

        ArgumentCaptor<ShopAdminMember> captor = ArgumentCaptor.forClass(ShopAdminMember.class);
        verify(memberRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(ShopAdminStatus.PENDING);
        assertThat(captor.getValue().getAdminId()).isEqualTo(10L);
        assertThat(captor.getValue().getCompanyId()).isEqualTo(7L);
        assertThat(captor.getValue().getRequestedRole()).isEqualTo(ShopRole.PARTNER_ADMIN);
        assertThat(captor.getValue().getRole()).isNull();
        assertThat(captor.getValue().getRequestedAt()).isEqualTo(NOW);
        assertThat(r.accessStatus()).isEqualTo(AccessStatus.PENDING);
    }

    @Test
    void 거절된_계정은_재신청할_수_있다() {
        ShopAdminMember rejected = member(ShopAdminStatus.REJECTED);
        given(identityVerifier.getAccount(10L)).willReturn(identity(7L, false));
        given(memberRepository.findByAdminId(10L)).willReturn(Optional.of(rejected));
        given(memberRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        MyAccess r = service.apply(10L, false);

        assertThat(r.accessStatus()).isEqualTo(AccessStatus.PENDING);
        assertThat(r.rejectReason()).isNull();
        assertThat(rejected.getRequestedAt()).isEqualTo(NOW);
    }

    @Test
    void 재신청하면_온우림의_현재_회사로_갱신된다() {
        ShopAdminMember rejected = member(ShopAdminStatus.REJECTED);
        given(identityVerifier.getAccount(10L)).willReturn(identity(8L, false));
        given(memberRepository.findByAdminId(10L)).willReturn(Optional.of(rejected));
        given(memberRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        service.apply(10L, false);

        assertThat(rejected.getCompanyId()).isEqualTo(8L);
    }

    @Test
    void 회사가_없는_계정은_신청할_수_없다() {
        given(identityVerifier.getAccount(10L)).willReturn(identity(null, false));

        assertCode(() -> service.apply(10L, false), ShopErrorCode.ACCESS_COMPANY_REQUIRED);
        verify(memberRepository, never()).save(any());
    }

    @Test
    void 온우림이_슈퍼관리자로_판정한_계정은_신청할_수_없다() {
        given(identityVerifier.getAccount(10L)).willReturn(identity(null, true));

        assertCode(() -> service.apply(10L, false), ShopErrorCode.ACCESS_NOT_REQUIRED);
        verify(memberRepository, never()).save(any());
    }

    @Test
    void 대기중이거나_승인된_계정의_신청은_거부된다() {
        given(identityVerifier.getAccount(10L)).willReturn(identity(7L, false));
        given(memberRepository.findByAdminId(10L))
            .willReturn(Optional.of(member(ShopAdminStatus.PENDING)), Optional.of(member(ShopAdminStatus.APPROVED)));

        assertCode(() -> service.apply(10L, false), ShopErrorCode.ACCESS_ALREADY_REQUESTED);
        assertCode(() -> service.apply(10L, false), ShopErrorCode.ACCESS_ALREADY_REQUESTED);
        verify(memberRepository, never()).save(any());
    }

    @Test
    void 슈퍼관리자는_신청할_수_없다() {
        assertCode(() -> service.apply(10L, true), ShopErrorCode.ACCESS_NOT_REQUIRED);
        verifyNoInteractions(identityVerifier);
        verify(memberRepository, never()).save(any());
    }

    @Test
    void 온우림_계정_확인에_실패하면_신청이_저장되지_않는다() {
        given(identityVerifier.getAccount(10L))
            .willThrow(new ShopBusinessException(ShopErrorCode.AUTH_ACCOUNT_INACTIVE));

        assertCode(() -> service.apply(10L, false), ShopErrorCode.AUTH_ACCOUNT_INACTIVE);
        verify(memberRepository, never()).save(any());
    }
}
