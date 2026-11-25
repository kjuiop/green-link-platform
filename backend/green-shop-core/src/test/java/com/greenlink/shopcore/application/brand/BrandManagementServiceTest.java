package com.greenlink.shopcore.application.brand;

import com.greenlink.shopcore.application.brand.service.BrandManagementService;
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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BrandManagementServiceTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-11-25T01:00:00Z"), ZoneId.of("Asia/Seoul"));
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 25, 10, 0);
    private static final ManagementActor SUPER = new ManagementActor(1L, null, ShopRole.SUPER_ADMIN);
    private static final ManagementActor PARTNER = new ManagementActor(50L, 7L, ShopRole.PARTNER_ADMIN);
    private static final ManagementActor BRAND_ADMIN = new ManagementActor(60L, 7L, ShopRole.BRAND_ADMIN);

    @Mock ShopBrandRepository brandRepository;
    @Mock ShopBrandMemberRepository brandMemberRepository;
    @Mock ShopAdminMemberRepository memberRepository;

    private BrandManagementService service;

    @BeforeEach
    void setUp() {
        service = new BrandManagementService(brandRepository, brandMemberRepository, memberRepository,
            new ManagementGuard(memberRepository), CLOCK);
    }

    private ShopAdminMember approved(Long id, Long adminId, Long companyId, ShopRole role) {
        return ShopAdminMember.of(id, adminId, companyId, role, role, ShopAdminStatus.APPROVED,
            NOW.minusDays(2), NOW.minusDays(1), 1L, null);
    }

    private void partnerIsApproved() {
        given(memberRepository.findByAdminId(50L)).willReturn(Optional.of(approved(5L, 50L, 7L, ShopRole.PARTNER_ADMIN)));
    }

    private ShopBrand brand(Long id, Long companyId, String name) {
        return ShopBrand.of(id, companyId, name, null);
    }

    private void assertCode(Runnable call, ErrorCode expected) {
        assertThatThrownBy(call::run).isInstanceOfSatisfying(ShopBusinessException.class,
            e -> assertThat(e.getErrorCode()).isEqualTo(expected));
    }

    @Test
    void 파트너_관리자는_자기_회사_브랜드만_조회한다() {
        partnerIsApproved();
        given(brandRepository.findAllByCompanyId(7L)).willReturn(List.of(brand(100L, 7L, "A")));

        assertThat(service.list(PARTNER, 999L)).extracting(ShopBrand::getId).containsExactly(100L);
        verify(brandRepository, never()).findAll();
    }

    @Test
    void 슈퍼관리자는_회사를_지정하면_해당_회사_없으면_전체를_조회한다() {
        given(brandRepository.findAllByCompanyId(8L)).willReturn(List.of(brand(200L, 8L, "B")));
        given(brandRepository.findAll()).willReturn(List.of(brand(100L, 7L, "A"), brand(200L, 8L, "B")));

        assertThat(service.list(SUPER, 8L)).hasSize(1);
        assertThat(service.list(SUPER, null)).hasSize(2);
    }

    @Test
    void 브랜드_관리자는_배정된_브랜드만_조회한다() {
        ShopAdminMember me = approved(6L, 60L, 7L, ShopRole.BRAND_ADMIN);
        given(memberRepository.findByAdminId(60L)).willReturn(Optional.of(me));
        given(brandMemberRepository.findBrandIdsByMemberId(6L)).willReturn(Set.of(101L));
        given(brandRepository.findAllByCompanyId(7L))
            .willReturn(List.of(brand(100L, 7L, "A"), brand(101L, 7L, "B")));

        assertThat(service.list(BRAND_ADMIN, null)).extracting(ShopBrand::getId).containsExactly(101L);
    }

    @Test
    void 구성원에서_제거된_브랜드_관리자는_조회할_수_없다() {
        ShopAdminMember removed = ShopAdminMember.of(6L, 60L, 7L, ShopRole.BRAND_ADMIN, ShopRole.BRAND_ADMIN,
            ShopAdminStatus.REMOVED, NOW.minusDays(3), NOW.minusDays(1), 1L, null);
        given(memberRepository.findByAdminId(60L)).willReturn(Optional.of(removed));

        assertCode(() -> service.list(BRAND_ADMIN, null), ShopErrorCode.ACCESS_MANAGE_FORBIDDEN);
    }

    @Test
    void 파트너_관리자는_요청의_회사_ID와_무관하게_자기_회사에_브랜드를_만든다() {
        partnerIsApproved();
        given(brandRepository.existsByCompanyIdAndName(7L, "새 브랜드")).willReturn(false);
        given(brandRepository.save(any())).willAnswer(i -> i.getArgument(0));

        ShopBrand created = service.create(PARTNER, 999L, " 새 브랜드 ");

        assertThat(created.getCompanyId()).isEqualTo(7L);
        assertThat(created.getName()).isEqualTo("새 브랜드");
    }

    @Test
    void 슈퍼관리자는_회사를_지정해야_브랜드를_만들_수_있다() {
        assertCode(() -> service.create(SUPER, null, "브랜드"), ShopErrorCode.BRAND_COMPANY_REQUIRED);

        given(brandRepository.existsByCompanyIdAndName(8L, "브랜드")).willReturn(false);
        given(brandRepository.save(any())).willAnswer(i -> i.getArgument(0));
        assertThat(service.create(SUPER, 8L, "브랜드").getCompanyId()).isEqualTo(8L);
    }

    @Test
    void 브랜드_관리자는_브랜드를_만들_수_없다() {
        assertCode(() -> service.create(BRAND_ADMIN, 7L, "브랜드"), ShopErrorCode.ACCESS_MANAGE_FORBIDDEN);
        verify(brandRepository, never()).save(any());
    }

    @Test
    void 같은_회사에_같은_이름이_있으면_거부된다() {
        partnerIsApproved();
        given(brandRepository.existsByCompanyIdAndName(7L, "중복")).willReturn(true);

        assertCode(() -> service.create(PARTNER, null, "중복"), ShopErrorCode.BRAND_NAME_DUPLICATED);
        verify(brandRepository, never()).save(any());
    }

    @Test
    void 빈_이름이나_100자_초과_이름은_거부된다() {
        partnerIsApproved();

        assertCode(() -> service.create(PARTNER, null, "  "), ShopErrorCode.BRAND_NAME_INVALID);
        assertCode(() -> service.create(PARTNER, null, "가".repeat(101)), ShopErrorCode.BRAND_NAME_INVALID);
    }

    @Test
    void 이름을_바꾸면_중복을_확인하고_저장한다() {
        partnerIsApproved();
        ShopBrand b = brand(100L, 7L, "옛 이름");
        given(brandRepository.findById(100L)).willReturn(Optional.of(b));
        given(brandRepository.existsByCompanyIdAndName(7L, "새 이름")).willReturn(false);
        given(brandRepository.save(b)).willReturn(b);

        assertThat(service.rename(PARTNER, 100L, "새 이름").getName()).isEqualTo("새 이름");
    }

    @Test
    void 같은_이름으로_수정해도_중복으로_취급하지_않는다() {
        partnerIsApproved();
        ShopBrand b = brand(100L, 7L, "그대로");
        given(brandRepository.findById(100L)).willReturn(Optional.of(b));
        given(brandRepository.save(b)).willReturn(b);

        service.rename(PARTNER, 100L, "그대로");

        verify(brandRepository, never()).existsByCompanyIdAndName(any(), any());
    }

    @Test
    void 파트너_관리자는_다른_회사_브랜드를_수정_삭제할_수_없다() {
        partnerIsApproved();
        given(brandRepository.findById(200L)).willReturn(Optional.of(brand(200L, 8L, "타사")));

        assertCode(() -> service.rename(PARTNER, 200L, "변경"), ShopErrorCode.ACCESS_MANAGE_FORBIDDEN);
        assertCode(() -> service.delete(PARTNER, 200L), ShopErrorCode.ACCESS_MANAGE_FORBIDDEN);
        verify(brandRepository, never()).save(any());
        verify(brandMemberRepository, never()).unassignAllByBrandId(any(), any());
    }

    @Test
    void 없거나_이미_삭제된_브랜드는_BRAND_NOT_FOUND() {
        given(brandRepository.findById(5L)).willReturn(Optional.empty());

        assertCode(() -> service.rename(SUPER, 5L, "x"), ShopErrorCode.BRAND_NOT_FOUND);
        assertCode(() -> service.delete(SUPER, 5L), ShopErrorCode.BRAND_NOT_FOUND);
    }

    @Test
    void 삭제는_소프트_삭제이고_배정도_소프트_해제된다() {
        partnerIsApproved();
        ShopBrand b = brand(100L, 7L, "A");
        given(brandRepository.findById(100L)).willReturn(Optional.of(b));

        service.delete(PARTNER, 100L);

        assertThat(b.isDeleted()).isTrue();
        assertThat(b.getDeletedAt()).isEqualTo(NOW);
        verify(brandRepository).save(b);
        verify(brandMemberRepository).unassignAllByBrandId(100L, NOW);
    }
}
