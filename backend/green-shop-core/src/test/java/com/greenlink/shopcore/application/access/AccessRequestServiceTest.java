package com.greenlink.shopcore.application.access;

import com.greenlink.shopcore.application.access.dto.MyAccess;
import com.greenlink.shopcore.application.access.service.AccessRequestService;
import com.greenlink.shopcore.application.auth.dto.VerifiedIdentity;
import com.greenlink.shopcore.application.auth.port.IdentityVerifier;
import com.greenlink.shopcore.application.auth.service.AccessEvaluator;
import com.greenlink.shopcore.domain.auth.AccessRequestType;
import com.greenlink.shopcore.domain.auth.AccessStatus;
import com.greenlink.shopcore.domain.auth.ShopRole;
import com.greenlink.shopcore.domain.brand.model.ShopBrand;
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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
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
    @Mock ShopBrandRepository brandRepository;
    @Mock IdentityVerifier identityVerifier;

    private AccessRequestService service;

    @BeforeEach
    void setUp() {
        service = new AccessRequestService(memberRepository, brandRepository, identityVerifier, new AccessEvaluator(memberRepository), CLOCK);
    }

    private ShopAdminMember member(ShopAdminStatus status) {
        return ShopAdminMember.of(1L, 10L, 7L, status == ShopAdminStatus.APPROVED ? ShopRole.PARTNER_ADMIN : null,
            ShopRole.PARTNER_ADMIN, status, NOW.minusDays(1), NOW.minusHours(1), 99L,
            status == ShopAdminStatus.REJECTED ? "사유" : null);
    }

    private VerifiedIdentity identity(Long companyId, boolean superAdmin) {
        return identity(companyId, superAdmin, false);
    }

    private VerifiedIdentity identity(Long companyId, boolean superAdmin, boolean siteEnabled) {
        return new VerifiedIdentity(10L, "login", "이름", companyId, companyId == null ? null : "회사",
            true, siteEnabled, true, superAdmin);
    }

    private void assertCode(Runnable call, ErrorCode expected) {
        assertThatThrownBy(call::run).isInstanceOfSatisfying(ShopBusinessException.class,
            e -> assertThat(e.getErrorCode()).isEqualTo(expected));
    }

    @Test
    void 내_상태_조회_슈퍼관리자는_APPROVED이고_저장소를_조회하지_않는다() {
        MyAccess r = service.getMyAccess(10L, true);

        assertThat(r.accessStatus()).isEqualTo(AccessStatus.APPROVED);
        verifyNoInteractions(memberRepository, identityVerifier);
    }

    @Test
    void 내_상태_조회_row가_없으면_NONE() {
        given(identityVerifier.getAccount(10L)).willReturn(identity(7L, false));
        given(memberRepository.findByAdminId(10L)).willReturn(Optional.empty());

        MyAccess r = service.getMyAccess(10L, false);

        assertThat(r.accessStatus()).isEqualTo(AccessStatus.NONE);
        assertThat(r.requestType()).isEqualTo(AccessRequestType.COMPANY);
    }

    @Test
    void 내_상태_조회_거절되면_사유가_함께_반환된다() {
        given(identityVerifier.getAccount(10L)).willReturn(identity(7L, false));
        given(memberRepository.findByAdminId(10L)).willReturn(Optional.of(member(ShopAdminStatus.REJECTED)));

        MyAccess r = service.getMyAccess(10L, false);

        assertThat(r.accessStatus()).isEqualTo(AccessStatus.REJECTED);
        assertThat(r.rejectReason()).isEqualTo("사유");
    }

    @Test
    void 내_상태_조회_회사가_다르면_NONE() {
        given(identityVerifier.getAccount(10L)).willReturn(identity(8L, false, true));
        given(memberRepository.findByAdminId(10L)).willReturn(Optional.of(member(ShopAdminStatus.APPROVED)));

        assertThat(service.getMyAccess(10L, false).accessStatus()).isEqualTo(AccessStatus.NONE);
    }

    @Test
    void 내_상태_조회_소속없는_합류_신청자는_회사가_달라도_PENDING() {
        given(identityVerifier.getAccount(10L)).willReturn(identity(null, false));
        given(memberRepository.findByAdminId(10L)).willReturn(Optional.of(member(ShopAdminStatus.PENDING)));

        MyAccess r = service.getMyAccess(10L, false);

        assertThat(r.accessStatus()).isEqualTo(AccessStatus.PENDING);
        assertThat(r.requestType()).isEqualTo(AccessRequestType.JOIN);
        assertThat(r.requestedRole()).isEqualTo(ShopRole.PARTNER_ADMIN);
    }

    @Test
    void 내_상태_조회_승인됐지만_회사_쇼핑몰이_꺼지면_SITE_DISABLED() {
        given(identityVerifier.getAccount(10L)).willReturn(identity(7L, false, false));
        given(memberRepository.findByAdminId(10L)).willReturn(Optional.of(member(ShopAdminStatus.APPROVED)));

        assertThat(service.getMyAccess(10L, false).accessStatus()).isEqualTo(AccessStatus.SITE_DISABLED);
    }

    @Test
    void 내_상태_조회는_토큰이_아닌_온우림의_최신_회사를_기준으로_한다() {
        given(identityVerifier.getAccount(10L)).willReturn(identity(7L, false, true));
        given(memberRepository.findByAdminId(10L)).willReturn(Optional.of(member(ShopAdminStatus.APPROVED)));

        assertThat(service.getMyAccess(10L, false).accessStatus()).isEqualTo(AccessStatus.APPROVED);
    }

    @Test
    void 처음_신청하면_회사_ID와_PARTNER_ADMIN_요청_역할로_PENDING_row가_저장된다() {
        given(identityVerifier.getAccount(10L)).willReturn(identity(7L, false));
        given(memberRepository.findByAdminId(10L)).willReturn(Optional.empty());
        given(memberRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        MyAccess r = service.apply(10L, false, null, null);

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

        MyAccess r = service.apply(10L, false, null, null);

        assertThat(r.accessStatus()).isEqualTo(AccessStatus.PENDING);
        assertThat(r.rejectReason()).isNull();
        assertThat(rejected.getRequestedAt()).isEqualTo(NOW);
    }

    @Test
    void 제거된_계정은_재신청할_수_있고_기존_역할은_초기화된다() {
        ShopAdminMember removed = ShopAdminMember.of(1L, 10L, 7L, ShopRole.PARTNER_ADMIN, ShopRole.PARTNER_ADMIN,
            ShopAdminStatus.REMOVED, NOW.minusDays(1), NOW.minusHours(1), 99L, null);
        given(identityVerifier.getAccount(10L)).willReturn(identity(7L, false));
        given(memberRepository.findByAdminId(10L)).willReturn(Optional.of(removed));
        given(memberRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        MyAccess r = service.apply(10L, false, null, null);

        assertThat(r.accessStatus()).isEqualTo(AccessStatus.PENDING);
        assertThat(removed.getRole()).isNull();
    }

    @Test
    void 재신청하면_온우림의_현재_회사로_갱신된다() {
        ShopAdminMember rejected = member(ShopAdminStatus.REJECTED);
        given(identityVerifier.getAccount(10L)).willReturn(identity(8L, false));
        given(memberRepository.findByAdminId(10L)).willReturn(Optional.of(rejected));
        given(memberRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        service.apply(10L, false, null, null);

        assertThat(rejected.getCompanyId()).isEqualTo(8L);
    }

    @Test
    void 회사가_없는_계정은_신청할_수_없다() {
        given(identityVerifier.getAccount(10L)).willReturn(identity(null, false));

        assertCode(() -> service.apply(10L, false, null, null), ShopErrorCode.ACCESS_COMPANY_REQUIRED);
        verify(memberRepository, never()).save(any());
    }

    @Test
    void 온우림이_슈퍼관리자로_판정한_계정은_신청할_수_없다() {
        given(identityVerifier.getAccount(10L)).willReturn(identity(null, true));

        assertCode(() -> service.apply(10L, false, null, null), ShopErrorCode.ACCESS_NOT_REQUIRED);
        verify(memberRepository, never()).save(any());
    }

    @Test
    void 대기중이거나_승인된_계정의_신청은_거부된다() {
        given(identityVerifier.getAccount(10L)).willReturn(identity(7L, false));
        given(memberRepository.findByAdminId(10L))
            .willReturn(Optional.of(member(ShopAdminStatus.PENDING)), Optional.of(member(ShopAdminStatus.APPROVED)));

        assertCode(() -> service.apply(10L, false, null, null), ShopErrorCode.ACCESS_ALREADY_REQUESTED);
        assertCode(() -> service.apply(10L, false, null, null), ShopErrorCode.ACCESS_ALREADY_REQUESTED);
        verify(memberRepository, never()).save(any());
    }

    @Test
    void 슈퍼관리자는_신청할_수_없다() {
        assertCode(() -> service.apply(10L, true, null, null), ShopErrorCode.ACCESS_NOT_REQUIRED);
        verifyNoInteractions(identityVerifier);
        verify(memberRepository, never()).save(any());
    }

    @Test
    void 온우림_계정_확인에_실패하면_신청이_저장되지_않는다() {
        given(identityVerifier.getAccount(10L))
            .willThrow(new ShopBusinessException(ShopErrorCode.AUTH_ACCOUNT_INACTIVE));

        assertCode(() -> service.apply(10L, false, null, null), ShopErrorCode.AUTH_ACCOUNT_INACTIVE);
        verify(memberRepository, never()).save(any());
    }

    @Test
    void 쇼핑몰이_꺼진_회사_신청은_요청_역할과_무관하게_PARTNER_ADMIN으로_고정된다() {
        given(identityVerifier.getAccount(10L)).willReturn(identity(7L, false, false));
        given(memberRepository.findByAdminId(10L)).willReturn(Optional.empty());
        given(memberRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        MyAccess r = service.apply(10L, false, ShopRole.BRAND_ADMIN, null);

        ArgumentCaptor<ShopAdminMember> captor = ArgumentCaptor.forClass(ShopAdminMember.class);
        verify(memberRepository).save(captor.capture());
        assertThat(captor.getValue().getRequestedRole()).isEqualTo(ShopRole.PARTNER_ADMIN);
        assertThat(r.requestType()).isEqualTo(AccessRequestType.COMPANY);
    }

    @Test
    void 합류_신청은_선택한_역할로_저장된다() {
        given(identityVerifier.getAccount(10L)).willReturn(identity(7L, false, true));
        given(memberRepository.findByAdminId(10L)).willReturn(Optional.empty());
        given(memberRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        MyAccess r = service.apply(10L, false, ShopRole.BRAND_ADMIN, null);

        ArgumentCaptor<ShopAdminMember> captor = ArgumentCaptor.forClass(ShopAdminMember.class);
        verify(memberRepository).save(captor.capture());
        assertThat(captor.getValue().getRequestedRole()).isEqualTo(ShopRole.BRAND_ADMIN);
        assertThat(r.requestType()).isEqualTo(AccessRequestType.JOIN);
        assertThat(r.requestedRole()).isEqualTo(ShopRole.BRAND_ADMIN);
    }

    @Test
    void 합류_신청에_역할이_없으면_거부되고_저장되지_않는다() {
        given(identityVerifier.getAccount(10L)).willReturn(identity(7L, false, true));
        given(memberRepository.findByAdminId(10L)).willReturn(Optional.empty());

        assertCode(() -> service.apply(10L, false, null, null), ShopErrorCode.ACCESS_ROLE_NOT_ASSIGNABLE);
        assertCode(() -> service.apply(10L, false, ShopRole.SUPER_ADMIN, null), ShopErrorCode.ACCESS_ROLE_NOT_ASSIGNABLE);
        verify(memberRepository, never()).save(any());
    }

    @Test
    void 소속없는_합류_신청자의_재신청은_기존_멤버의_회사와_요청_역할을_유지한다() {
        ShopAdminMember rejected = ShopAdminMember.of(1L, 10L, 7L, null, ShopRole.BRAND_ADMIN,
            ShopAdminStatus.REJECTED, NOW.minusDays(1), NOW.minusHours(1), 99L, "사유");
        given(identityVerifier.getAccount(10L)).willReturn(identity(null, false, false));
        given(memberRepository.findByAdminId(10L)).willReturn(Optional.of(rejected));
        given(memberRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        MyAccess r = service.apply(10L, false, null, null);

        assertThat(rejected.getCompanyId()).isEqualTo(7L);
        assertThat(rejected.getRequestedRole()).isEqualTo(ShopRole.BRAND_ADMIN);
        assertThat(r.accessStatus()).isEqualTo(AccessStatus.PENDING);
        assertThat(r.requestType()).isEqualTo(AccessRequestType.JOIN);
    }

    private void joinWithBrands(Long... brandIds) {
        given(identityVerifier.getAccount(10L)).willReturn(identity(7L, false, true));
        given(memberRepository.findByAdminId(10L)).willReturn(Optional.empty());
        List<ShopBrand> brands = new ArrayList<>();
        for (Long id : brandIds) brands.add(ShopBrand.of(id, 7L, "브랜드" + id, null));
        given(brandRepository.findAllByCompanyId(7L)).willReturn(brands);
    }

    @Test
    void 합류_브랜드관리자_신청은_희망_브랜드가_저장되고_승인_전이라_역할은_부여되지_않는다() {
        joinWithBrands(1L, 2L, 3L);
        given(memberRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        MyAccess r = service.apply(10L, false, ShopRole.BRAND_ADMIN, Set.of(1L, 3L));

        ArgumentCaptor<ShopAdminMember> captor = ArgumentCaptor.forClass(ShopAdminMember.class);
        verify(memberRepository).save(captor.capture());
        assertThat(captor.getValue().getRequestedBrandIds()).containsExactlyInAnyOrder(1L, 3L);
        assertThat(captor.getValue().getRole()).isNull();
        assertThat(r.requestedBrandIds()).containsExactlyInAnyOrder(1L, 3L);
    }

    @Test
    void 회사에_브랜드가_없으면_브랜드_없이_신청할_수_있다() {
        joinWithBrands();
        given(memberRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        MyAccess r = service.apply(10L, false, ShopRole.BRAND_ADMIN, Set.of(99L));

        assertThat(r.accessStatus()).isEqualTo(AccessStatus.PENDING);
        assertThat(r.requestedBrandIds()).isEmpty();
    }

    @Test
    void 회사에_브랜드가_있는데_선택하지_않으면_거부된다() {
        joinWithBrands(1L);

        assertCode(() -> service.apply(10L, false, ShopRole.BRAND_ADMIN, Set.of()), ShopErrorCode.MEMBER_BRAND_REQUIRED);
        assertCode(() -> service.apply(10L, false, ShopRole.BRAND_ADMIN, null), ShopErrorCode.MEMBER_BRAND_REQUIRED);
        verify(memberRepository, never()).save(any());
    }

    @Test
    void 다른_회사_또는_삭제된_브랜드는_거부된다() {
        joinWithBrands(1L);

        assertCode(() -> service.apply(10L, false, ShopRole.BRAND_ADMIN, Set.of(1L, 55L)), ShopErrorCode.MEMBER_BRAND_INVALID);
        verify(memberRepository, never()).save(any());
    }

    @Test
    void 파트너관리자_신청은_브랜드_값을_무시한다() {
        given(identityVerifier.getAccount(10L)).willReturn(identity(7L, false, true));
        given(memberRepository.findByAdminId(10L)).willReturn(Optional.empty());
        given(memberRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        MyAccess r = service.apply(10L, false, ShopRole.PARTNER_ADMIN, Set.of(1L));

        assertThat(r.requestedBrandIds()).isEmpty();
        verifyNoInteractions(brandRepository);
    }

    @Test
    void 회사_최초_연동_신청은_브랜드_값을_무시한다() {
        given(identityVerifier.getAccount(10L)).willReturn(identity(7L, false, false));
        given(memberRepository.findByAdminId(10L)).willReturn(Optional.empty());
        given(memberRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        MyAccess r = service.apply(10L, false, ShopRole.BRAND_ADMIN, Set.of(1L));

        assertThat(r.requestedRole()).isEqualTo(ShopRole.PARTNER_ADMIN);
        assertThat(r.requestedBrandIds()).isEmpty();
        verifyNoInteractions(brandRepository);
    }

    @Test
    void 재신청하면_희망_브랜드를_덮어쓴다() {
        ShopAdminMember rejected = ShopAdminMember.of(1L, 10L, 7L, null, ShopRole.BRAND_ADMIN, Set.of(1L),
            ShopAdminStatus.REJECTED, NOW.minusDays(1), NOW.minusHours(1), 99L, "사유");
        given(identityVerifier.getAccount(10L)).willReturn(identity(7L, false, true));
        given(memberRepository.findByAdminId(10L)).willReturn(Optional.of(rejected));
        given(brandRepository.findAllByCompanyId(7L))
            .willReturn(List.of(ShopBrand.of(1L, 7L, "a", null), ShopBrand.of(2L, 7L, "b", null)));
        given(memberRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        MyAccess r = service.apply(10L, false, ShopRole.BRAND_ADMIN, Set.of(2L));

        assertThat(r.requestedBrandIds()).containsExactly(2L);
    }

    @Test
    void 내_상태_조회에_희망_브랜드가_포함된다() {
        ShopAdminMember pending = ShopAdminMember.of(1L, 10L, 7L, null, ShopRole.BRAND_ADMIN, Set.of(4L, 5L),
            ShopAdminStatus.PENDING, NOW.minusDays(1), null, null, null);
        given(identityVerifier.getAccount(10L)).willReturn(identity(7L, false, true));
        given(memberRepository.findByAdminId(10L)).willReturn(Optional.of(pending));

        assertThat(service.getMyAccess(10L, false).requestedBrandIds()).containsExactlyInAnyOrder(4L, 5L);
    }

    @Test
    void 신청자용_브랜드_목록은_본인_회사의_브랜드만_반환한다() {
        given(identityVerifier.getAccount(10L)).willReturn(identity(7L, false, true));
        given(brandRepository.findAllByCompanyId(7L)).willReturn(List.of(ShopBrand.of(1L, 7L, "꽃집", null)));

        assertThat(service.getApplicableBrands(10L, false)).extracting("id", "name").containsExactly(tuple(1L, "꽃집"));
    }

    @Test
    void 신청자용_브랜드_목록은_슈퍼관리자와_회사없는_계정에는_비어있다() {
        assertThat(service.getApplicableBrands(10L, true)).isEmpty();

        given(identityVerifier.getAccount(10L)).willReturn(identity(null, false));
        given(memberRepository.findByAdminId(10L)).willReturn(Optional.empty());
        assertThat(service.getApplicableBrands(10L, false)).isEmpty();
        verifyNoInteractions(brandRepository);
    }
}
