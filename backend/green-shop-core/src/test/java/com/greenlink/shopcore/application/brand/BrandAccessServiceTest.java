package com.greenlink.shopcore.application.brand;

import com.greenlink.shopcore.application.brand.service.BrandAccessService;
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

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class BrandAccessServiceTest {

    @Mock ShopBrandRepository brandRepository;
    @Mock ShopBrandMemberRepository brandMemberRepository;
    @Mock ShopAdminMemberRepository memberRepository;

    private BrandAccessService service;

    @BeforeEach
    void setUp() {
        service = new BrandAccessService(brandRepository, brandMemberRepository, memberRepository);
    }

    private void assertCode(Runnable call, ErrorCode expected) {
        assertThatThrownBy(call::run).isInstanceOfSatisfying(ShopBusinessException.class,
            e -> assertThat(e.getErrorCode()).isEqualTo(expected));
    }

    private ShopAdminMember brandAdminMember() {
        return ShopAdminMember.of(5L, 10L, 7L, ShopRole.BRAND_ADMIN, ShopRole.BRAND_ADMIN,
            ShopAdminStatus.APPROVED, LocalDateTime.of(2026, 11, 23, 10, 0), null, null, null);
    }

    @Test
    void 없는_브랜드는_BRAND_NOT_FOUND() {
        given(brandRepository.findById(100L)).willReturn(Optional.empty());

        assertCode(() -> service.requireAccess(10L, ShopRole.SUPER_ADMIN, null, 100L), ShopErrorCode.BRAND_NOT_FOUND);
    }

    @Test
    void 슈퍼관리자는_배정_조회_없이_통과한다() {
        given(brandRepository.findById(100L)).willReturn(Optional.of(ShopBrand.of(100L, 7L, "브랜드", null)));

        assertThatCode(() -> service.requireAccess(1L, ShopRole.SUPER_ADMIN, null, 100L)).doesNotThrowAnyException();
        verifyNoInteractions(memberRepository, brandMemberRepository);
    }

    @Test
    void 파트너_관리자는_자기_회사_브랜드면_배정_조회_없이_통과한다() {
        given(brandRepository.findById(100L)).willReturn(Optional.of(ShopBrand.of(100L, 7L, "브랜드", null)));

        assertThatCode(() -> service.requireAccess(10L, ShopRole.PARTNER_ADMIN, 7L, 100L)).doesNotThrowAnyException();
        verifyNoInteractions(memberRepository, brandMemberRepository);
    }

    @Test
    void 파트너_관리자가_다른_회사_브랜드에_접근하면_거부된다() {
        given(brandRepository.findById(100L)).willReturn(Optional.of(ShopBrand.of(100L, 7L, "브랜드", null)));

        assertCode(() -> service.requireAccess(10L, ShopRole.PARTNER_ADMIN, 8L, 100L), ShopErrorCode.BRAND_ACCESS_DENIED);
    }

    @Test
    void 브랜드_관리자는_배정된_브랜드에_접근할_수_있다() {
        given(brandRepository.findById(100L)).willReturn(Optional.of(ShopBrand.of(100L, 7L, "브랜드", null)));
        given(memberRepository.findByAdminId(10L)).willReturn(Optional.of(brandAdminMember()));
        given(brandMemberRepository.findBrandIdsByMemberId(5L)).willReturn(Set.of(100L));

        assertThatCode(() -> service.requireAccess(10L, ShopRole.BRAND_ADMIN, 7L, 100L)).doesNotThrowAnyException();
    }

    @Test
    void 브랜드_관리자는_배정되지_않은_브랜드에_접근하면_거부된다() {
        given(brandRepository.findById(100L)).willReturn(Optional.of(ShopBrand.of(100L, 7L, "브랜드", null)));
        given(memberRepository.findByAdminId(10L)).willReturn(Optional.of(brandAdminMember()));
        given(brandMemberRepository.findBrandIdsByMemberId(5L)).willReturn(Set.of(101L));

        assertCode(() -> service.requireAccess(10L, ShopRole.BRAND_ADMIN, 7L, 100L), ShopErrorCode.BRAND_ACCESS_DENIED);
    }

    @Test
    void 멤버_행이_없는_브랜드_관리자는_거부된다() {
        given(brandRepository.findById(100L)).willReturn(Optional.of(ShopBrand.of(100L, 7L, "브랜드", null)));
        given(memberRepository.findByAdminId(10L)).willReturn(Optional.empty());

        assertCode(() -> service.requireAccess(10L, ShopRole.BRAND_ADMIN, 7L, 100L), ShopErrorCode.BRAND_ACCESS_DENIED);
        verifyNoInteractions(brandMemberRepository);
    }
}
