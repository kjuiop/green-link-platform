package com.greenlink.shopcore.application.admin;

import com.greenlink.shopcore.application.access.port.CompanyAccessGranter;
import com.greenlink.shopcore.application.admin.dto.ShopAdminView;
import com.greenlink.shopcore.application.admin.service.ShopAdminManagementService;
import com.greenlink.shopcore.application.auth.dto.AccountProfile;
import com.greenlink.shopcore.application.auth.dto.ManagedAccountCommand;
import com.greenlink.shopcore.application.auth.dto.VerifiedIdentity;
import com.greenlink.shopcore.application.auth.port.IdentityRegistrar;
import com.greenlink.shopcore.application.auth.port.IdentityVerifier;
import com.greenlink.shopcore.application.admin.dto.CreateMemberCommand;
import com.greenlink.shopcore.application.admin.service.MemberApprovalWriter;
import com.greenlink.shopcore.application.common.ManagementActor;
import com.greenlink.shopcore.application.common.ManagementGuard;
import com.greenlink.shopcore.domain.auth.ShopRole;
import com.greenlink.shopcore.domain.brand.model.ShopBrand;
import com.greenlink.shopcore.domain.brand.port.ShopBrandMemberRepository;
import com.greenlink.shopcore.domain.brand.port.ShopBrandRepository;
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
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
class ShopAdminManagementServiceTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-11-23T01:00:00Z"), ZoneId.of("Asia/Seoul"));
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 23, 10, 0);

    @Mock ShopAdminMemberRepository memberRepository;
    @Mock ShopBrandRepository brandRepository;
    @Mock ShopBrandMemberRepository brandMemberRepository;
    @Mock IdentityVerifier identityVerifier;
    @Mock IdentityRegistrar identityRegistrar;
    @Mock CompanyAccessGranter accessGranter;

    private ShopAdminManagementService service;

    @BeforeEach
    void setUp() {
        service = new ShopAdminManagementService(memberRepository, brandRepository, brandMemberRepository,
            identityVerifier, identityRegistrar, accessGranter, new ManagementGuard(memberRepository),
            new MemberApprovalWriter(memberRepository, brandMemberRepository), CLOCK);
    }

    private static final ManagementActor SUPER = new ManagementActor(99L, null, ShopRole.SUPER_ADMIN);
    private static final ManagementActor PARTNER = new ManagementActor(50L, 7L, ShopRole.PARTNER_ADMIN);
    private static final ManagementActor BRAND_ADMIN_ACTOR = new ManagementActor(60L, 7L, ShopRole.BRAND_ADMIN);

    private ShopAdminMember member(ShopAdminStatus status) {
        return ShopAdminMember.of(1L, 10L, 7L, status == ShopAdminStatus.APPROVED ? ShopRole.PARTNER_ADMIN : null,
            ShopRole.PARTNER_ADMIN, status, NOW.minusDays(1), null, null, null);
    }

    private ShopBrand brand(Long id, Long companyId) {
        return ShopBrand.of(id, companyId, "브랜드" + id, null);
    }

    private ShopAdminMember approvedMember(Long id, Long adminId, Long companyId, ShopRole role) {
        return ShopAdminMember.of(id, adminId, companyId, role, role, ShopAdminStatus.APPROVED,
            NOW.minusDays(2), NOW.minusDays(1), 99L, null);
    }

    private ShopAdminMember pendingMember(Long id, Long adminId, Long companyId, ShopRole requested) {
        return ShopAdminMember.of(id, adminId, companyId, null, requested, ShopAdminStatus.PENDING,
            NOW.minusDays(1), null, null, null);
    }

    private void partnerActorIsApproved() {
        given(memberRepository.findByAdminId(50L))
            .willReturn(Optional.of(approvedMember(5L, 50L, 7L, ShopRole.PARTNER_ADMIN)));
    }

    private void assertCode(Runnable call, ErrorCode expected) {
        assertThatThrownBy(call::run).isInstanceOfSatisfying(ShopBusinessException.class,
            e -> assertThat(e.getErrorCode()).isEqualTo(expected));
    }

    @Test
    void 목록은_상태_필터를_저장소에_전달하고_온우림_배치_조회로_이름을_채운다() {
        given(memberRepository.findAll(ShopAdminStatus.PENDING)).willReturn(List.of(member(ShopAdminStatus.PENDING)));
        given(identityVerifier.getAccounts(List.of(10L)))
            .willReturn(List.of(new AccountProfile(10L, "login", "이름", "회사", false, false, false)));

        List<ShopAdminView> result = service.list(SUPER, ShopAdminStatus.PENDING, null);

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

        assertThat(service.list(SUPER, null, null).get(0).role()).isEqualTo(ShopRole.PARTNER_ADMIN);
    }

    @Test
    void 온우림에_없는_계정은_이름_없이_반환된다() {
        given(memberRepository.findAll(null)).willReturn(List.of(member(ShopAdminStatus.PENDING)));
        given(identityVerifier.getAccounts(List.of(10L))).willReturn(List.of());

        ShopAdminView v = service.list(SUPER, null, null).get(0);

        assertThat(v.name()).isNull();
        assertThat(v.loginId()).isNull();
        assertThat(v.companyName()).isNull();
    }

    @Test
    void 온우림_조회가_실패해도_목록은_이름_없이_반환된다() {
        given(memberRepository.findAll(null)).willReturn(List.of(member(ShopAdminStatus.PENDING)));
        given(identityVerifier.getAccounts(any()))
            .willThrow(new ShopBusinessException(ShopErrorCode.AUTH_UPSTREAM_UNAVAILABLE));

        List<ShopAdminView> result = service.list(SUPER, null, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isNull();
        assertThat(result.get(0).status()).isEqualTo(ShopAdminStatus.PENDING);
    }

    @Test
    void 목록이_비어_있으면_온우림을_조회하지_않는다() {
        given(memberRepository.findAll(null)).willReturn(List.of());

        assertThat(service.list(SUPER, null, null)).isEmpty();
        verifyNoInteractions(identityVerifier);
    }

    @Test
    void 승인하면_온우림_권한부여_후_처리자와_시각이_기록되어_저장된다() {
        ShopAdminMember m = member(ShopAdminStatus.PENDING);
        given(memberRepository.findById(1L)).willReturn(Optional.of(m));

        service.approve(SUPER, 1L, null, null);

        assertThat(m.getStatus()).isEqualTo(ShopAdminStatus.APPROVED);
        assertThat(m.getRole()).isEqualTo(ShopRole.PARTNER_ADMIN);
        assertThat(m.getProcessedBy()).isEqualTo(99L);
        assertThat(m.getProcessedAt()).isEqualTo(NOW);
        InOrder order = inOrder(accessGranter, memberRepository);
        order.verify(accessGranter).grant(10L, 7L, ShopRole.PARTNER_ADMIN);
        order.verify(memberRepository).save(m);
    }

    @Test
    void 신청보다_낮은_역할로_승인할_수_있다() {
        ShopAdminMember m = member(ShopAdminStatus.PENDING);
        given(memberRepository.findById(1L)).willReturn(Optional.of(m));
        given(memberRepository.existsApprovedByCompanyId(7L)).willReturn(true);
        given(brandRepository.findAllByCompanyId(7L)).willReturn(List.of(brand(100L, 7L)));

        service.approve(SUPER, 1L, ShopRole.BRAND_ADMIN, Set.of(100L));

        assertThat(m.getRole()).isEqualTo(ShopRole.BRAND_ADMIN);
        verify(accessGranter).grant(10L, 7L, ShopRole.BRAND_ADMIN);
        verify(memberRepository).save(m);
    }

    @Test
    void 신청보다_높은_역할로의_승인은_온우림_호출_없이_거부된다() {
        ShopAdminMember m = ShopAdminMember.request(10L, 7L, ShopRole.BRAND_ADMIN, NOW.minusDays(1));
        given(memberRepository.findById(1L)).willReturn(Optional.of(m));

        assertCode(() -> service.approve(SUPER, 1L, ShopRole.PARTNER_ADMIN, null), ShopErrorCode.ACCESS_ROLE_EXCEEDS_REQUEST);
        verifyNoInteractions(accessGranter);
        verify(memberRepository, never()).save(any());
    }

    @Test
    void 슈퍼관리자_역할로의_승인은_거부된다() {
        given(memberRepository.findById(1L)).willReturn(Optional.of(member(ShopAdminStatus.PENDING)));

        assertCode(() -> service.approve(SUPER, 1L, ShopRole.SUPER_ADMIN, null), ShopErrorCode.ACCESS_ROLE_NOT_ASSIGNABLE);
        verifyNoInteractions(accessGranter);
    }

    @Test
    void 회사의_첫_승인_멤버가_브랜드_관리자면_거부된다() {
        ShopAdminMember m = member(ShopAdminStatus.PENDING);
        given(memberRepository.findById(1L)).willReturn(Optional.of(m));
        given(memberRepository.existsApprovedByCompanyId(7L)).willReturn(false);

        assertCode(() -> service.approve(SUPER, 1L, ShopRole.BRAND_ADMIN, null), ShopErrorCode.ACCESS_FIRST_MEMBER_MUST_BE_PARTNER);
        verifyNoInteractions(accessGranter);
        verify(memberRepository, never()).save(any());
    }

    @Test
    void 이미_승인된_멤버가_있는_회사는_브랜드_관리자로_승인할_수_있다() {
        ShopAdminMember m = member(ShopAdminStatus.PENDING);
        given(memberRepository.findById(1L)).willReturn(Optional.of(m));
        given(memberRepository.existsApprovedByCompanyId(7L)).willReturn(true);
        given(brandRepository.findAllByCompanyId(7L)).willReturn(List.of(brand(100L, 7L)));

        service.approve(SUPER, 1L, ShopRole.BRAND_ADMIN, Set.of(100L));

        assertThat(m.getRole()).isEqualTo(ShopRole.BRAND_ADMIN);
    }

    @Test
    void 온우림_권한부여가_실패하면_멤버는_PENDING으로_남고_저장되지_않는다() {
        ShopAdminMember m = member(ShopAdminStatus.PENDING);
        given(memberRepository.findById(1L)).willReturn(Optional.of(m));
        doThrow(new ShopBusinessException(ShopErrorCode.AUTH_UPSTREAM_UNAVAILABLE)).when(accessGranter).grant(10L, 7L, ShopRole.PARTNER_ADMIN);

        assertCode(() -> service.approve(SUPER, 1L, null, null), ShopErrorCode.AUTH_UPSTREAM_UNAVAILABLE);

        assertThat(m.getStatus()).isEqualTo(ShopAdminStatus.PENDING);
        assertThat(m.getRole()).isNull();
        verify(memberRepository, never()).save(any());
    }

    @Test
    void 온우림_실패_후_재시도하면_승인된다() {
        ShopAdminMember m = member(ShopAdminStatus.PENDING);
        given(memberRepository.findById(1L)).willReturn(Optional.of(m));
        doThrow(new ShopBusinessException(ShopErrorCode.AUTH_UPSTREAM_UNAVAILABLE))
            .doNothing().when(accessGranter).grant(10L, 7L, ShopRole.PARTNER_ADMIN);

        assertCode(() -> service.approve(SUPER, 1L, null, null), ShopErrorCode.AUTH_UPSTREAM_UNAVAILABLE);
        service.approve(SUPER, 1L, null, null);

        assertThat(m.getStatus()).isEqualTo(ShopAdminStatus.APPROVED);
        verify(memberRepository).save(m);
    }

    @Test
    void 목록은_소속없는_합류_신청자의_회사명을_회사_배치_조회로_채운다() {
        given(memberRepository.findAll(null)).willReturn(List.of(member(ShopAdminStatus.PENDING)));
        given(identityVerifier.getAccounts(List.of(10L)))
            .willReturn(List.of(new AccountProfile(10L, "login", "이름", null, false, false, false)));
        given(identityVerifier.getCompanyNames(List.of(7L))).willReturn(Map.of(7L, "신청 회사"));

        assertThat(service.list(SUPER, null, null).get(0).companyName()).isEqualTo("신청 회사");
    }

    @Test
    void 회사명_조회가_실패해도_목록은_반환된다() {
        given(memberRepository.findAll(null)).willReturn(List.of(member(ShopAdminStatus.PENDING)));
        given(identityVerifier.getAccounts(List.of(10L)))
            .willReturn(List.of(new AccountProfile(10L, "login", "이름", null, false, false, false)));
        given(identityVerifier.getCompanyNames(any()))
            .willThrow(new ShopBusinessException(ShopErrorCode.AUTH_UPSTREAM_UNAVAILABLE));

        ShopAdminView v = service.list(SUPER, null, null).get(0);

        assertThat(v.loginId()).isEqualTo("login");
        assertThat(v.companyName()).isNull();
    }

    @Test
    void 거절하면_사유가_기록되어_저장된다() {
        ShopAdminMember m = member(ShopAdminStatus.PENDING);
        given(memberRepository.findById(1L)).willReturn(Optional.of(m));

        service.reject(SUPER, 1L, "서류 미비");

        assertThat(m.getStatus()).isEqualTo(ShopAdminStatus.REJECTED);
        assertThat(m.getRejectReason()).isEqualTo("서류 미비");
        verify(memberRepository).save(m);
    }

    @Test
    void 사유_없는_거절은_거부되고_저장되지_않는다() {
        given(memberRepository.findById(1L)).willReturn(Optional.of(member(ShopAdminStatus.PENDING)));

        assertCode(() -> service.reject(SUPER, 1L, " "), ShopErrorCode.ACCESS_REJECT_REASON_REQUIRED);
        verify(memberRepository, never()).save(any());
    }

    @Test
    void 이미_처리된_건은_승인_거절이_거부된다() {
        given(memberRepository.findById(1L))
            .willReturn(Optional.of(member(ShopAdminStatus.APPROVED)), Optional.of(member(ShopAdminStatus.REJECTED)));

        assertCode(() -> service.approve(SUPER, 1L, null, null), ShopErrorCode.ACCESS_INVALID_TRANSITION);
        assertCode(() -> service.reject(SUPER, 1L, "사유"), ShopErrorCode.ACCESS_INVALID_TRANSITION);
        verify(memberRepository, never()).save(any());
        verifyNoInteractions(accessGranter);
    }

    @Test
    void 없는_id는_ADMIN_REQUEST_NOT_FOUND() {
        given(memberRepository.findById(5L)).willReturn(Optional.empty());

        assertCode(() -> service.approve(SUPER, 5L, null, null), ShopErrorCode.ADMIN_REQUEST_NOT_FOUND);
        assertCode(() -> service.reject(SUPER, 5L, "사유"), ShopErrorCode.ADMIN_REQUEST_NOT_FOUND);
    }

    @Test
    void 브랜드_관리자_승인은_브랜드를_지정하지_않으면_온우림_호출_없이_거부된다() {
        given(memberRepository.findById(1L)).willReturn(Optional.of(pendingMember(1L, 10L, 7L, ShopRole.BRAND_ADMIN)));
        given(memberRepository.existsApprovedByCompanyId(7L)).willReturn(true);

        assertCode(() -> service.approve(SUPER, 1L, null, null), ShopErrorCode.MEMBER_BRAND_REQUIRED);
        assertCode(() -> service.approve(SUPER, 1L, null, Set.of()), ShopErrorCode.MEMBER_BRAND_REQUIRED);
        verifyNoInteractions(accessGranter);
        verify(memberRepository, never()).save(any());
    }

    @Test
    void 다른_회사나_삭제된_브랜드는_지정할_수_없다() {
        given(memberRepository.findById(1L)).willReturn(Optional.of(pendingMember(1L, 10L, 7L, ShopRole.BRAND_ADMIN)));
        given(memberRepository.existsApprovedByCompanyId(7L)).willReturn(true);
        given(brandRepository.findAllByCompanyId(7L)).willReturn(List.of(brand(100L, 7L)));

        assertCode(() -> service.approve(SUPER, 1L, null, Set.of(100L, 200L)), ShopErrorCode.MEMBER_BRAND_INVALID);
        verifyNoInteractions(accessGranter);
    }

    @Test
    void 브랜드_관리자_승인은_브랜드_배정까지_함께_저장한다() {
        ShopAdminMember m = pendingMember(1L, 10L, 7L, ShopRole.BRAND_ADMIN);
        given(memberRepository.findById(1L)).willReturn(Optional.of(m));
        given(memberRepository.existsApprovedByCompanyId(7L)).willReturn(true);
        given(brandRepository.findAllByCompanyId(7L)).willReturn(List.of(brand(100L, 7L), brand(101L, 7L)));

        service.approve(SUPER, 1L, null, Set.of(100L, 101L));

        InOrder order = inOrder(accessGranter, memberRepository, brandMemberRepository);
        order.verify(accessGranter).grant(10L, 7L, ShopRole.BRAND_ADMIN);
        order.verify(memberRepository).save(m);
        order.verify(brandMemberRepository).replaceAssignments(1L, Set.of(100L, 101L), NOW);
    }

    @Test
    void 승인_때_지정한_브랜드가_신청자의_희망_브랜드보다_우선해_배정된다() {
        ShopAdminMember m = ShopAdminMember.of(1L, 10L, 7L, null, ShopRole.BRAND_ADMIN, Set.of(100L),
            ShopAdminStatus.PENDING, NOW.minusDays(1), null, null, null);
        given(memberRepository.findById(1L)).willReturn(Optional.of(m));
        given(memberRepository.existsApprovedByCompanyId(7L)).willReturn(true);
        given(brandRepository.findAllByCompanyId(7L)).willReturn(List.of(brand(100L, 7L), brand(101L, 7L)));

        service.approve(SUPER, 1L, null, Set.of(101L));

        verify(brandMemberRepository).replaceAssignments(1L, Set.of(101L), NOW);
    }

    @Test
    void 파트너_관리자_승인은_브랜드_배정을_비운다() {
        ShopAdminMember m = pendingMember(1L, 10L, 7L, ShopRole.PARTNER_ADMIN);
        given(memberRepository.findById(1L)).willReturn(Optional.of(m));

        service.approve(SUPER, 1L, null, Set.of(100L));

        verify(brandMemberRepository).replaceAssignments(1L, Set.of(), NOW);
        verifyNoInteractions(brandRepository);
    }

    @Test
    void 파트너_관리자는_같은_회사_신청을_브랜드_관리자로_승인할_수_있고_처리자가_기록된다() {
        partnerActorIsApproved();
        ShopAdminMember m = pendingMember(1L, 10L, 7L, ShopRole.BRAND_ADMIN);
        given(memberRepository.findById(1L)).willReturn(Optional.of(m));
        given(memberRepository.existsApprovedByCompanyId(7L)).willReturn(true);
        given(brandRepository.findAllByCompanyId(7L)).willReturn(List.of(brand(100L, 7L)));

        service.approve(PARTNER, 1L, ShopRole.BRAND_ADMIN, Set.of(100L));

        assertThat(m.getStatus()).isEqualTo(ShopAdminStatus.APPROVED);
        assertThat(m.getProcessedBy()).isEqualTo(50L);
    }

    @Test
    void 파트너_관리자는_같은_회사의_파트너_관리자_신청을_승인할_수_있다() {
        partnerActorIsApproved();
        ShopAdminMember m = pendingMember(1L, 10L, 7L, ShopRole.PARTNER_ADMIN);
        given(memberRepository.findById(1L)).willReturn(Optional.of(m));

        service.approve(PARTNER, 1L, null, null);

        assertThat(m.getStatus()).isEqualTo(ShopAdminStatus.APPROVED);
        assertThat(m.getRole()).isEqualTo(ShopRole.PARTNER_ADMIN);
        assertThat(m.getProcessedBy()).isEqualTo(50L);
        verify(accessGranter).grant(10L, 7L, ShopRole.PARTNER_ADMIN);
    }

    @Test
    void 파트너_관리자는_다른_회사_신청을_처리할_수_없다() {
        partnerActorIsApproved();
        given(memberRepository.findById(1L)).willReturn(Optional.of(pendingMember(1L, 10L, 8L, ShopRole.BRAND_ADMIN)));

        assertCode(() -> service.approve(PARTNER, 1L, ShopRole.BRAND_ADMIN, Set.of(100L)), ShopErrorCode.ACCESS_MANAGE_FORBIDDEN);
        assertCode(() -> service.reject(PARTNER, 1L, "사유"), ShopErrorCode.ACCESS_MANAGE_FORBIDDEN);
        assertCode(() -> service.remove(PARTNER, 1L), ShopErrorCode.ACCESS_MANAGE_FORBIDDEN);
        assertCode(() -> service.changeRole(PARTNER, 1L, ShopRole.PARTNER_ADMIN), ShopErrorCode.ACCESS_MANAGE_FORBIDDEN);
        assertCode(() -> service.assignBrands(PARTNER, 1L, Set.of()), ShopErrorCode.ACCESS_MANAGE_FORBIDDEN);
        verifyNoInteractions(accessGranter);
        verify(memberRepository, never()).save(any());
    }

    @Test
    void 파트너_관리자는_같은_회사_신청을_거절할_수_있다() {
        partnerActorIsApproved();
        ShopAdminMember m = pendingMember(1L, 10L, 7L, ShopRole.BRAND_ADMIN);
        given(memberRepository.findById(1L)).willReturn(Optional.of(m));

        service.reject(PARTNER, 1L, "부적합");

        assertThat(m.getStatus()).isEqualTo(ShopAdminStatus.REJECTED);
        assertThat(m.getProcessedBy()).isEqualTo(50L);
    }

    @Test
    void 브랜드_관리자와_승인되지_않은_파트너는_구성원_API를_쓸_수_없다() {
        assertCode(() -> service.list(BRAND_ADMIN_ACTOR, null, null), ShopErrorCode.ACCESS_MANAGE_FORBIDDEN);
        assertCode(() -> service.approve(BRAND_ADMIN_ACTOR, 1L, null, null), ShopErrorCode.ACCESS_MANAGE_FORBIDDEN);
        assertCode(() -> service.remove(BRAND_ADMIN_ACTOR, 1L), ShopErrorCode.ACCESS_MANAGE_FORBIDDEN);

        given(memberRepository.findByAdminId(50L)).willReturn(Optional.empty());
        assertCode(() -> service.list(PARTNER, null, null), ShopErrorCode.ACCESS_MANAGE_FORBIDDEN);
        verify(memberRepository, never()).save(any());
    }

    @Test
    void 구성원에서_제거된_파트너의_남은_토큰으로는_관리할_수_없다() {
        ShopAdminMember removed = ShopAdminMember.of(5L, 50L, 7L, ShopRole.PARTNER_ADMIN, ShopRole.PARTNER_ADMIN,
            ShopAdminStatus.REMOVED, NOW.minusDays(3), NOW.minusDays(1), 99L, null);
        given(memberRepository.findByAdminId(50L)).willReturn(Optional.of(removed));

        assertCode(() -> service.list(PARTNER, null, null), ShopErrorCode.ACCESS_MANAGE_FORBIDDEN);
    }

    @Test
    void 슈퍼관리자는_회사_조건으로_목록을_좁힐_수_있고_조건이_없으면_전체를_조회한다() {
        ShopAdminMember m = approvedMember(1L, 10L, 8L, ShopRole.BRAND_ADMIN);
        given(memberRepository.findAllByCompanyId(8L, null)).willReturn(List.of(m));
        given(identityVerifier.getAccounts(List.of(10L))).willReturn(List.of());
        given(brandMemberRepository.findBrandIdsByMemberIds(List.of(1L))).willReturn(Map.of());

        assertThat(service.list(SUPER, null, 8L)).hasSize(1);
        verify(memberRepository, never()).findAll(any());

        given(memberRepository.findAll(null)).willReturn(List.of());
        assertThat(service.list(SUPER, null, null)).isEmpty();
    }

    @Test
    void 파트너_관리자는_다른_회사를_조건으로_넘겨도_자기_회사만_조회한다() {
        partnerActorIsApproved();
        given(memberRepository.findAllByCompanyId(7L, null)).willReturn(List.of());

        assertThat(service.list(PARTNER, null, 999L)).isEmpty();
        verify(memberRepository, never()).findAllByCompanyId(eq(999L), any());
    }

    @Test
    void 파트너_관리자의_목록은_자기_회사_구성원만_조회하고_브랜드_배정을_포함한다() {
        partnerActorIsApproved();
        ShopAdminMember m = approvedMember(1L, 10L, 7L, ShopRole.BRAND_ADMIN);
        given(memberRepository.findAllByCompanyId(7L, null)).willReturn(List.of(m));
        given(identityVerifier.getAccounts(List.of(10L))).willReturn(List.of());
        given(brandMemberRepository.findBrandIdsByMemberIds(List.of(1L))).willReturn(Map.of(1L, Set.of(100L)));

        List<ShopAdminView> result = service.list(PARTNER, null, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).brandIds()).containsExactly(100L);
        verify(memberRepository, never()).findAll(any());
    }

    @Test
    void 파트너_관리자는_같은_회사_구성원의_역할을_변경할_수_있다() {
        partnerActorIsApproved();
        ShopAdminMember m = approvedMember(1L, 10L, 7L, ShopRole.BRAND_ADMIN);
        given(memberRepository.findById(1L)).willReturn(Optional.of(m));

        service.changeRole(PARTNER, 1L, ShopRole.PARTNER_ADMIN);

        assertThat(m.getRole()).isEqualTo(ShopRole.PARTNER_ADMIN);
        InOrder order = inOrder(accessGranter, memberRepository);
        order.verify(accessGranter).changeRole(10L, ShopRole.PARTNER_ADMIN);
        order.verify(memberRepository).save(m);
        verify(brandMemberRepository).unassignAllByMemberId(1L, NOW);
    }

    @Test
    void 슈퍼관리자가_브랜드_관리자를_파트너로_승격하면_브랜드_배정이_해제된다() {
        ShopAdminMember m = approvedMember(1L, 10L, 7L, ShopRole.BRAND_ADMIN);
        given(memberRepository.findById(1L)).willReturn(Optional.of(m));

        service.changeRole(SUPER, 1L, ShopRole.PARTNER_ADMIN);

        assertThat(m.getRole()).isEqualTo(ShopRole.PARTNER_ADMIN);
        verify(memberRepository).save(m);
        verify(brandMemberRepository).unassignAllByMemberId(1L, NOW);
    }

    @Test
    void 마지막_파트너는_강등할_수_없다() {
        ShopAdminMember m = approvedMember(1L, 10L, 7L, ShopRole.PARTNER_ADMIN);
        given(memberRepository.findById(1L)).willReturn(Optional.of(m));
        given(memberRepository.countApprovedByCompanyIdAndRole(7L, ShopRole.PARTNER_ADMIN)).willReturn(1L);

        assertCode(() -> service.changeRole(SUPER, 1L, ShopRole.BRAND_ADMIN), ShopErrorCode.MEMBER_LAST_PARTNER);
        verify(memberRepository, never()).save(any());
        verifyNoInteractions(accessGranter);
    }

    @Test
    void 온우림_role_동기화가_실패하면_멤버_role은_저장되지_않는다() {
        ShopAdminMember m = approvedMember(1L, 10L, 7L, ShopRole.BRAND_ADMIN);
        given(memberRepository.findById(1L)).willReturn(Optional.of(m));
        doThrow(new ShopBusinessException(ShopErrorCode.AUTH_UPSTREAM_UNAVAILABLE))
            .when(accessGranter).changeRole(10L, ShopRole.PARTNER_ADMIN);

        assertCode(() -> service.changeRole(SUPER, 1L, ShopRole.PARTNER_ADMIN), ShopErrorCode.AUTH_UPSTREAM_UNAVAILABLE);

        verify(memberRepository, never()).save(any());
        verify(brandMemberRepository, never()).unassignAllByMemberId(any(), any());
    }

    @Test
    void 슈퍼관리자_역할로는_변경할_수_없고_온우림도_호출하지_않는다() {
        ShopAdminMember m = approvedMember(1L, 10L, 7L, ShopRole.BRAND_ADMIN);
        given(memberRepository.findById(1L)).willReturn(Optional.of(m));

        assertCode(() -> service.changeRole(SUPER, 1L, ShopRole.SUPER_ADMIN), ShopErrorCode.ACCESS_ROLE_NOT_ASSIGNABLE);

        verify(memberRepository, never()).save(any());
        verifyNoInteractions(accessGranter);
    }

    @Test
    void 파트너가_둘_이상이면_강등할_수_있다() {
        ShopAdminMember m = approvedMember(1L, 10L, 7L, ShopRole.PARTNER_ADMIN);
        given(memberRepository.findById(1L)).willReturn(Optional.of(m));
        given(memberRepository.countApprovedByCompanyIdAndRole(7L, ShopRole.PARTNER_ADMIN)).willReturn(2L);

        service.changeRole(SUPER, 1L, ShopRole.BRAND_ADMIN);

        assertThat(m.getRole()).isEqualTo(ShopRole.BRAND_ADMIN);
        verify(accessGranter).changeRole(10L, ShopRole.BRAND_ADMIN);
        verify(memberRepository).save(m);
        verify(brandMemberRepository, never()).unassignAllByMemberId(any(), any());
    }

    @Test
    void 승인되지_않은_구성원의_역할은_바꿀_수_없다() {
        given(memberRepository.findById(1L)).willReturn(Optional.of(pendingMember(1L, 10L, 7L, ShopRole.BRAND_ADMIN)));

        assertCode(() -> service.changeRole(SUPER, 1L, ShopRole.PARTNER_ADMIN), ShopErrorCode.MEMBER_NOT_APPROVED);
    }

    @Test
    void 브랜드_배정은_전체_교체로_저장된다() {
        partnerActorIsApproved();
        given(memberRepository.findById(1L)).willReturn(Optional.of(approvedMember(1L, 10L, 7L, ShopRole.BRAND_ADMIN)));
        given(brandRepository.findAllByCompanyId(7L)).willReturn(List.of(brand(100L, 7L), brand(101L, 7L)));

        service.assignBrands(PARTNER, 1L, Set.of(101L));

        verify(brandMemberRepository).replaceAssignments(1L, Set.of(101L), NOW);
    }

    @Test
    void 브랜드_배정은_빈_집합으로_전체_해제할_수_있다() {
        given(memberRepository.findById(1L)).willReturn(Optional.of(approvedMember(1L, 10L, 7L, ShopRole.BRAND_ADMIN)));
        given(brandRepository.findAllByCompanyId(7L)).willReturn(List.of(brand(100L, 7L)));

        service.assignBrands(SUPER, 1L, Set.of());

        verify(brandMemberRepository).replaceAssignments(1L, Set.of(), NOW);
    }

    @Test
    void 타_회사_브랜드나_파트너_대상_배정은_거부된다() {
        given(memberRepository.findById(1L)).willReturn(
            Optional.of(approvedMember(1L, 10L, 7L, ShopRole.BRAND_ADMIN)),
            Optional.of(approvedMember(2L, 11L, 7L, ShopRole.PARTNER_ADMIN)),
            Optional.of(pendingMember(3L, 12L, 7L, ShopRole.BRAND_ADMIN)));
        given(brandRepository.findAllByCompanyId(7L)).willReturn(List.of(brand(100L, 7L)));

        assertCode(() -> service.assignBrands(SUPER, 1L, Set.of(999L)), ShopErrorCode.MEMBER_BRAND_INVALID);
        assertCode(() -> service.assignBrands(SUPER, 1L, Set.of(100L)), ShopErrorCode.MEMBER_BRAND_INVALID);
        assertCode(() -> service.assignBrands(SUPER, 1L, Set.of(100L)), ShopErrorCode.MEMBER_NOT_APPROVED);
        verify(brandMemberRepository, never()).replaceAssignments(any(), any(), any());
    }

    @Test
    void 구성원_삭제는_소프트_삭제이고_브랜드_배정도_소프트_해제된다() {
        ShopAdminMember m = approvedMember(1L, 10L, 7L, ShopRole.BRAND_ADMIN);
        given(memberRepository.findById(1L)).willReturn(Optional.of(m));

        service.remove(SUPER, 1L);

        assertThat(m.getStatus()).isEqualTo(ShopAdminStatus.REMOVED);
        assertThat(m.getProcessedBy()).isEqualTo(99L);
        verify(memberRepository).save(m);
        verify(brandMemberRepository).unassignAllByMemberId(1L, NOW);
    }

    @Test
    void 마지막_파트너는_삭제할_수_없다() {
        ShopAdminMember m = approvedMember(1L, 10L, 7L, ShopRole.PARTNER_ADMIN);
        given(memberRepository.findById(1L)).willReturn(Optional.of(m));
        given(memberRepository.countApprovedByCompanyIdAndRole(7L, ShopRole.PARTNER_ADMIN)).willReturn(1L);

        assertCode(() -> service.remove(SUPER, 1L), ShopErrorCode.MEMBER_LAST_PARTNER);
        assertThat(m.getStatus()).isEqualTo(ShopAdminStatus.APPROVED);
        verify(memberRepository, never()).save(any());
    }

    @Test
    void 파트너_관리자는_같은_회사의_다른_파트너를_삭제할_수_있다() {
        partnerActorIsApproved();
        ShopAdminMember other = approvedMember(2L, 11L, 7L, ShopRole.PARTNER_ADMIN);
        given(memberRepository.findById(2L)).willReturn(Optional.of(other));
        given(memberRepository.countApprovedByCompanyIdAndRole(7L, ShopRole.PARTNER_ADMIN)).willReturn(2L);

        service.remove(PARTNER, 2L);

        assertThat(other.getStatus()).isEqualTo(ShopAdminStatus.REMOVED);
        assertThat(other.getProcessedBy()).isEqualTo(50L);
        verify(memberRepository).save(other);
    }

    @Test
    void 파트너_관리자도_마지막_파트너인_자기_자신은_삭제할_수_없다() {
        partnerActorIsApproved();
        ShopAdminMember self = approvedMember(5L, 50L, 7L, ShopRole.PARTNER_ADMIN);
        given(memberRepository.findById(5L)).willReturn(Optional.of(self));
        given(memberRepository.countApprovedByCompanyIdAndRole(7L, ShopRole.PARTNER_ADMIN)).willReturn(1L);

        assertCode(() -> service.remove(PARTNER, 5L), ShopErrorCode.MEMBER_LAST_PARTNER);
        verify(memberRepository, never()).save(any());
    }

    @Test
    void 이미_삭제된_구성원은_다시_삭제할_수_없다() {
        ShopAdminMember removed = ShopAdminMember.of(1L, 10L, 7L, ShopRole.BRAND_ADMIN, ShopRole.BRAND_ADMIN,
            ShopAdminStatus.REMOVED, NOW.minusDays(3), NOW.minusDays(1), 99L, null);
        given(memberRepository.findById(1L)).willReturn(Optional.of(removed));

        assertCode(() -> service.remove(SUPER, 1L), ShopErrorCode.ACCESS_INVALID_TRANSITION);
        verify(memberRepository, never()).save(any());
    }

    private CreateMemberCommand createCmd(ShopRole role, Long companyId, Set<Long> brandIds) {
        return new CreateMemberCommand("brand01", "홍길동", "010-1234-5678", null, role, companyId, brandIds);
    }

    private void accountWillBeCreated() {
        given(identityRegistrar.createManagedAccount(any(ManagedAccountCommand.class)))
            .willReturn(new VerifiedIdentity(70L, "brand01", "홍길동", 7L, "회사", false, true, false, false));
        given(memberRepository.save(any(ShopAdminMember.class))).willAnswer(inv -> {
            ShopAdminMember m = inv.getArgument(0);
            return ShopAdminMember.of(77L, m.getAdminId(), m.getCompanyId(), m.getRole(), m.getRequestedRole(),
                m.getStatus(), m.getRequestedAt(), m.getProcessedAt(), m.getProcessedBy(), null);
        });
    }

    @Test
    void 파트너는_자기_회사에_브랜드_관리자를_만들고_브랜드가_배정된다() {
        partnerActorIsApproved();
        given(memberRepository.existsApprovedByCompanyId(7L)).willReturn(true);
        given(brandRepository.findAllByCompanyId(7L)).willReturn(List.of(brand(1L, 7L), brand(2L, 7L)));
        accountWillBeCreated();

        Long id = service.create(PARTNER, createCmd(ShopRole.BRAND_ADMIN, 999L, Set.of(1L)));

        assertThat(id).isEqualTo(77L);
        verify(identityRegistrar).createManagedAccount(
            new ManagedAccountCommand("brand01", "홍길동", "010-1234-5678", null, 7L, ShopRole.BRAND_ADMIN));
        verify(brandMemberRepository).replaceAssignments(eq(77L), eq(Set.of(1L)), any());
        org.mockito.ArgumentCaptor<ShopAdminMember> captor = org.mockito.ArgumentCaptor.forClass(ShopAdminMember.class);
        verify(memberRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(ShopAdminStatus.APPROVED);
        assertThat(captor.getValue().getRole()).isEqualTo(ShopRole.BRAND_ADMIN);
        assertThat(captor.getValue().getCompanyId()).isEqualTo(7L);
        assertThat(captor.getValue().getProcessedBy()).isEqualTo(50L);
    }

    @Test
    void 슈퍼관리자는_회사를_지정해_파트너_관리자를_만들면_브랜드_배정은_비어있다() {
        accountWillBeCreated();

        service.create(SUPER, createCmd(ShopRole.PARTNER_ADMIN, 7L, Set.of(1L)));

        verify(identityRegistrar).createManagedAccount(
            new ManagedAccountCommand("brand01", "홍길동", "010-1234-5678", null, 7L, ShopRole.PARTNER_ADMIN));
        verify(brandMemberRepository).replaceAssignments(eq(77L), eq(Set.of()), any());
    }

    @Test
    void 슈퍼관리자는_회사를_지정해야_한다() {
        assertCode(() -> service.create(SUPER, createCmd(ShopRole.PARTNER_ADMIN, null, null)),
            ShopErrorCode.MEMBER_COMPANY_REQUIRED);
        verifyNoInteractions(identityRegistrar);
    }

    @Test
    void 브랜드_관리자는_계정을_만들_수_없다() {
        assertCode(() -> service.create(BRAND_ADMIN_ACTOR, createCmd(ShopRole.BRAND_ADMIN, 7L, Set.of(1L))),
            ShopErrorCode.ACCESS_MANAGE_FORBIDDEN);
        verifyNoInteractions(identityRegistrar);
    }

    @Test
    void 슈퍼관리자_역할은_부여할_수_없다() {
        partnerActorIsApproved();

        assertCode(() -> service.create(PARTNER, createCmd(ShopRole.SUPER_ADMIN, null, null)),
            ShopErrorCode.ACCESS_ROLE_NOT_ASSIGNABLE);
        assertCode(() -> service.create(PARTNER, createCmd(null, null, null)),
            ShopErrorCode.ACCESS_ROLE_NOT_ASSIGNABLE);
        verifyNoInteractions(identityRegistrar);
    }

    @Test
    void 회사의_첫_멤버는_파트너_관리자여야_한다() {
        given(memberRepository.existsApprovedByCompanyId(7L)).willReturn(false);

        assertCode(() -> service.create(SUPER, createCmd(ShopRole.BRAND_ADMIN, 7L, Set.of(1L))),
            ShopErrorCode.ACCESS_FIRST_MEMBER_MUST_BE_PARTNER);
        verifyNoInteractions(identityRegistrar);
    }

    @Test
    void 브랜드_관리자는_브랜드를_1개_이상_지정해야_한다() {
        partnerActorIsApproved();
        given(memberRepository.existsApprovedByCompanyId(7L)).willReturn(true);

        assertCode(() -> service.create(PARTNER, createCmd(ShopRole.BRAND_ADMIN, null, Set.of())),
            ShopErrorCode.MEMBER_BRAND_REQUIRED);
        verifyNoInteractions(identityRegistrar);
    }

    @Test
    void 다른_회사_브랜드는_지정할_수_없고_계정도_만들지_않는다() {
        partnerActorIsApproved();
        given(memberRepository.existsApprovedByCompanyId(7L)).willReturn(true);
        given(brandRepository.findAllByCompanyId(7L)).willReturn(List.of(brand(1L, 7L)));

        assertCode(() -> service.create(PARTNER, createCmd(ShopRole.BRAND_ADMIN, null, Set.of(1L, 55L))),
            ShopErrorCode.MEMBER_BRAND_INVALID);
        verifyNoInteractions(identityRegistrar);
    }

    @Test
    void 아이디가_중복이면_멤버를_저장하지_않는다() {
        partnerActorIsApproved();
        given(identityRegistrar.createManagedAccount(any(ManagedAccountCommand.class)))
            .willThrow(new ShopBusinessException(ShopErrorCode.SIGNUP_LOGIN_ID_DUPLICATED));

        assertCode(() -> service.create(PARTNER, createCmd(ShopRole.PARTNER_ADMIN, null, null)),
            ShopErrorCode.SIGNUP_LOGIN_ID_DUPLICATED);
        verify(memberRepository, never()).save(any());
    }

    @Test
    void 파트너는_자기_회사_멤버의_비밀번호_설정을_재발급한다() {
        partnerActorIsApproved();
        given(memberRepository.findById(8L)).willReturn(Optional.of(approvedMember(8L, 80L, 7L, ShopRole.BRAND_ADMIN)));

        service.reissuePasswordSetup(PARTNER, 8L);

        verify(identityRegistrar).reissuePasswordSetup(80L);
    }

    @Test
    void 다른_회사_멤버의_재발급은_거부한다() {
        partnerActorIsApproved();
        given(memberRepository.findById(8L)).willReturn(Optional.of(approvedMember(8L, 80L, 8L, ShopRole.BRAND_ADMIN)));

        assertCode(() -> service.reissuePasswordSetup(PARTNER, 8L), ShopErrorCode.ACCESS_MANAGE_FORBIDDEN);
        verifyNoInteractions(identityRegistrar);
    }

    @Test
    void 승인되지_않은_멤버의_재발급은_거부한다() {
        given(memberRepository.findById(8L)).willReturn(Optional.of(pendingMember(8L, 80L, 7L, ShopRole.BRAND_ADMIN)));

        assertCode(() -> service.reissuePasswordSetup(SUPER, 8L), ShopErrorCode.MEMBER_NOT_APPROVED);
        verifyNoInteractions(identityRegistrar);
    }

    @Test
    void 브랜드_관리자는_재발급할_수_없다() {
        assertCode(() -> service.reissuePasswordSetup(BRAND_ADMIN_ACTOR, 8L), ShopErrorCode.ACCESS_MANAGE_FORBIDDEN);
        verifyNoInteractions(identityRegistrar);
    }

    @Test
    void 목록은_비밀번호_설정_대기_여부를_노출한다() {
        given(memberRepository.findAll(null)).willReturn(List.of(member(ShopAdminStatus.APPROVED)));
        given(identityVerifier.getAccounts(List.of(10L)))
            .willReturn(List.of(new AccountProfile(10L, "login", "이름", "회사", true, false, false)));

        assertThat(service.list(SUPER, null, null).get(0).passwordSetupPending()).isTrue();
    }

    @Test
    void 파트너는_자기_회사_멤버의_온우림_접근을_켜고_끈다() {
        partnerActorIsApproved();
        given(memberRepository.findById(8L)).willReturn(Optional.of(approvedMember(8L, 80L, 7L, ShopRole.BRAND_ADMIN)));

        service.changeOnurimAccess(PARTNER, 8L, true);
        service.changeOnurimAccess(PARTNER, 8L, false);

        verify(accessGranter).changeOnurimAccess(80L, 7L, true);
        verify(accessGranter).changeOnurimAccess(80L, 7L, false);
    }

    @Test
    void 슈퍼관리자도_온우림_접근을_변경할_수_있다() {
        given(memberRepository.findById(8L)).willReturn(Optional.of(approvedMember(8L, 80L, 7L, ShopRole.BRAND_ADMIN)));

        service.changeOnurimAccess(SUPER, 8L, true);

        verify(accessGranter).changeOnurimAccess(80L, 7L, true);
    }

    @Test
    void 다른_회사_멤버의_온우림_접근_변경은_거부한다() {
        partnerActorIsApproved();
        given(memberRepository.findById(8L)).willReturn(Optional.of(approvedMember(8L, 80L, 8L, ShopRole.BRAND_ADMIN)));

        assertCode(() -> service.changeOnurimAccess(PARTNER, 8L, true), ShopErrorCode.ACCESS_MANAGE_FORBIDDEN);
        verifyNoInteractions(accessGranter);
    }

    @Test
    void 승인되지_않은_멤버의_온우림_접근_변경은_거부한다() {
        given(memberRepository.findById(8L)).willReturn(Optional.of(pendingMember(8L, 80L, 7L, ShopRole.BRAND_ADMIN)));

        assertCode(() -> service.changeOnurimAccess(SUPER, 8L, true), ShopErrorCode.MEMBER_NOT_APPROVED);
        verifyNoInteractions(accessGranter);
    }

    @Test
    void 브랜드_관리자는_온우림_접근을_변경할_수_없다() {
        assertCode(() -> service.changeOnurimAccess(BRAND_ADMIN_ACTOR, 8L, true), ShopErrorCode.ACCESS_MANAGE_FORBIDDEN);
        verifyNoInteractions(accessGranter);
    }

    @Test
    void 없는_멤버의_온우림_접근_변경은_404() {
        given(memberRepository.findById(8L)).willReturn(Optional.empty());

        assertCode(() -> service.changeOnurimAccess(SUPER, 8L, true), ShopErrorCode.ADMIN_REQUEST_NOT_FOUND);
        verifyNoInteractions(accessGranter);
    }

    @Test
    void 목록은_온우림_접근_여부를_노출한다() {
        given(memberRepository.findAll(null)).willReturn(List.of(member(ShopAdminStatus.APPROVED)));
        given(identityVerifier.getAccounts(List.of(10L)))
            .willReturn(List.of(new AccountProfile(10L, "login", "이름", "회사", false, true, true)));

        var view = service.list(SUPER, null, null).get(0);

        assertThat(view.onurimEnabled()).isTrue();
        assertThat(view.companyOnurimEnabled()).isTrue();
    }
}
