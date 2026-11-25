package com.greenlink.shopcore.application.admin;

import com.greenlink.shopcore.application.access.port.CompanyAccessGranter;
import com.greenlink.shopcore.application.admin.dto.ShopAdminView;
import com.greenlink.shopcore.application.admin.service.ShopAdminManagementService;
import com.greenlink.shopcore.application.auth.dto.AccountProfile;
import com.greenlink.shopcore.application.auth.port.IdentityVerifier;
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
    @Mock CompanyAccessGranter accessGranter;

    private ShopAdminManagementService service;

    @BeforeEach
    void setUp() {
        service = new ShopAdminManagementService(memberRepository, brandRepository, brandMemberRepository,
            identityVerifier, accessGranter, new ManagementGuard(memberRepository),
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
            .willReturn(List.of(new AccountProfile(10L, "login", "이름", "회사")));

        List<ShopAdminView> result = service.list(SUPER, ShopAdminStatus.PENDING);

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

        assertThat(service.list(SUPER, null).get(0).role()).isEqualTo(ShopRole.PARTNER_ADMIN);
    }

    @Test
    void 온우림에_없는_계정은_이름_없이_반환된다() {
        given(memberRepository.findAll(null)).willReturn(List.of(member(ShopAdminStatus.PENDING)));
        given(identityVerifier.getAccounts(List.of(10L))).willReturn(List.of());

        ShopAdminView v = service.list(SUPER, null).get(0);

        assertThat(v.name()).isNull();
        assertThat(v.loginId()).isNull();
        assertThat(v.companyName()).isNull();
    }

    @Test
    void 온우림_조회가_실패해도_목록은_이름_없이_반환된다() {
        given(memberRepository.findAll(null)).willReturn(List.of(member(ShopAdminStatus.PENDING)));
        given(identityVerifier.getAccounts(any()))
            .willThrow(new ShopBusinessException(ShopErrorCode.AUTH_UPSTREAM_UNAVAILABLE));

        List<ShopAdminView> result = service.list(SUPER, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isNull();
        assertThat(result.get(0).status()).isEqualTo(ShopAdminStatus.PENDING);
    }

    @Test
    void 목록이_비어_있으면_온우림을_조회하지_않는다() {
        given(memberRepository.findAll(null)).willReturn(List.of());

        assertThat(service.list(SUPER, null)).isEmpty();
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
        order.verify(accessGranter).grant(10L, 7L);
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
        doThrow(new ShopBusinessException(ShopErrorCode.AUTH_UPSTREAM_UNAVAILABLE)).when(accessGranter).grant(10L, 7L);

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
            .doNothing().when(accessGranter).grant(10L, 7L);

        assertCode(() -> service.approve(SUPER, 1L, null, null), ShopErrorCode.AUTH_UPSTREAM_UNAVAILABLE);
        service.approve(SUPER, 1L, null, null);

        assertThat(m.getStatus()).isEqualTo(ShopAdminStatus.APPROVED);
        verify(memberRepository).save(m);
    }

    @Test
    void 목록은_소속없는_합류_신청자의_회사명을_회사_배치_조회로_채운다() {
        given(memberRepository.findAll(null)).willReturn(List.of(member(ShopAdminStatus.PENDING)));
        given(identityVerifier.getAccounts(List.of(10L)))
            .willReturn(List.of(new AccountProfile(10L, "login", "이름", null)));
        given(identityVerifier.getCompanyNames(List.of(7L))).willReturn(Map.of(7L, "신청 회사"));

        assertThat(service.list(SUPER, null).get(0).companyName()).isEqualTo("신청 회사");
    }

    @Test
    void 회사명_조회가_실패해도_목록은_반환된다() {
        given(memberRepository.findAll(null)).willReturn(List.of(member(ShopAdminStatus.PENDING)));
        given(identityVerifier.getAccounts(List.of(10L)))
            .willReturn(List.of(new AccountProfile(10L, "login", "이름", null)));
        given(identityVerifier.getCompanyNames(any()))
            .willThrow(new ShopBusinessException(ShopErrorCode.AUTH_UPSTREAM_UNAVAILABLE));

        ShopAdminView v = service.list(SUPER, null).get(0);

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
        order.verify(accessGranter).grant(10L, 7L);
        order.verify(memberRepository).save(m);
        order.verify(brandMemberRepository).replaceAssignments(1L, Set.of(100L, 101L), NOW);
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
    void 파트너_관리자는_파트너_관리자로_승인할_수_없다() {
        partnerActorIsApproved();
        given(memberRepository.findById(1L)).willReturn(Optional.of(pendingMember(1L, 10L, 7L, ShopRole.PARTNER_ADMIN)));

        assertCode(() -> service.approve(PARTNER, 1L, ShopRole.PARTNER_ADMIN, null),
            ShopErrorCode.ACCESS_PARTNER_PROMOTION_FORBIDDEN);
        assertCode(() -> service.approve(PARTNER, 1L, null, null), ShopErrorCode.ACCESS_PARTNER_PROMOTION_FORBIDDEN);
        verifyNoInteractions(accessGranter);
        verify(memberRepository, never()).save(any());
    }

    @Test
    void 파트너_관리자는_다른_회사_신청을_처리할_수_없다() {
        partnerActorIsApproved();
        given(memberRepository.findById(1L)).willReturn(Optional.of(pendingMember(1L, 10L, 8L, ShopRole.BRAND_ADMIN)));

        assertCode(() -> service.approve(PARTNER, 1L, ShopRole.BRAND_ADMIN, Set.of(100L)), ShopErrorCode.ACCESS_MANAGE_FORBIDDEN);
        assertCode(() -> service.reject(PARTNER, 1L, "사유"), ShopErrorCode.ACCESS_MANAGE_FORBIDDEN);
        assertCode(() -> service.remove(PARTNER, 1L), ShopErrorCode.ACCESS_MANAGE_FORBIDDEN);
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
        assertCode(() -> service.list(BRAND_ADMIN_ACTOR, null), ShopErrorCode.ACCESS_MANAGE_FORBIDDEN);
        assertCode(() -> service.approve(BRAND_ADMIN_ACTOR, 1L, null, null), ShopErrorCode.ACCESS_MANAGE_FORBIDDEN);
        assertCode(() -> service.remove(BRAND_ADMIN_ACTOR, 1L), ShopErrorCode.ACCESS_MANAGE_FORBIDDEN);

        given(memberRepository.findByAdminId(50L)).willReturn(Optional.empty());
        assertCode(() -> service.list(PARTNER, null), ShopErrorCode.ACCESS_MANAGE_FORBIDDEN);
        verify(memberRepository, never()).save(any());
    }

    @Test
    void 구성원에서_제거된_파트너의_남은_토큰으로는_관리할_수_없다() {
        ShopAdminMember removed = ShopAdminMember.of(5L, 50L, 7L, ShopRole.PARTNER_ADMIN, ShopRole.PARTNER_ADMIN,
            ShopAdminStatus.REMOVED, NOW.minusDays(3), NOW.minusDays(1), 99L, null);
        given(memberRepository.findByAdminId(50L)).willReturn(Optional.of(removed));

        assertCode(() -> service.list(PARTNER, null), ShopErrorCode.ACCESS_MANAGE_FORBIDDEN);
    }

    @Test
    void 파트너_관리자의_목록은_자기_회사_구성원만_조회하고_브랜드_배정을_포함한다() {
        partnerActorIsApproved();
        ShopAdminMember m = approvedMember(1L, 10L, 7L, ShopRole.BRAND_ADMIN);
        given(memberRepository.findAllByCompanyId(7L, null)).willReturn(List.of(m));
        given(identityVerifier.getAccounts(List.of(10L))).willReturn(List.of());
        given(brandMemberRepository.findBrandIdsByMemberIds(List.of(1L))).willReturn(Map.of(1L, Set.of(100L)));

        List<ShopAdminView> result = service.list(PARTNER, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).brandIds()).containsExactly(100L);
        verify(memberRepository, never()).findAll(any());
    }

    @Test
    void 역할_변경은_슈퍼관리자만_할_수_있다() {
        partnerActorIsApproved();

        assertCode(() -> service.changeRole(PARTNER, 1L, ShopRole.PARTNER_ADMIN), ShopErrorCode.ACCESS_MANAGE_FORBIDDEN);
        verify(memberRepository, never()).save(any());
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
    }

    @Test
    void 파트너가_둘_이상이면_강등할_수_있다() {
        ShopAdminMember m = approvedMember(1L, 10L, 7L, ShopRole.PARTNER_ADMIN);
        given(memberRepository.findById(1L)).willReturn(Optional.of(m));
        given(memberRepository.countApprovedByCompanyIdAndRole(7L, ShopRole.PARTNER_ADMIN)).willReturn(2L);

        service.changeRole(SUPER, 1L, ShopRole.BRAND_ADMIN);

        assertThat(m.getRole()).isEqualTo(ShopRole.BRAND_ADMIN);
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
    void 파트너_관리자는_다른_파트너를_삭제할_수_없다() {
        partnerActorIsApproved();
        given(memberRepository.findById(2L)).willReturn(Optional.of(approvedMember(2L, 11L, 7L, ShopRole.PARTNER_ADMIN)));

        assertCode(() -> service.remove(PARTNER, 2L), ShopErrorCode.ACCESS_MANAGE_FORBIDDEN);
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
}
