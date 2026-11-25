package com.greenlink.shopcore.application.brand.service;

import com.greenlink.shopcore.application.brand.usecase.BrandAccessUseCase;
import com.greenlink.shopcore.domain.auth.ShopAccessPolicy;
import com.greenlink.shopcore.domain.auth.ShopRole;
import com.greenlink.shopcore.domain.brand.model.ShopBrand;
import com.greenlink.shopcore.domain.brand.port.ShopBrandMemberRepository;
import com.greenlink.shopcore.domain.brand.port.ShopBrandRepository;
import com.greenlink.shopcore.domain.member.port.ShopAdminMemberRepository;
import com.greenlink.shopcore.global.exception.ShopBusinessException;
import com.greenlink.shopcore.global.exception.ShopErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@Transactional(readOnly = true)
public class BrandAccessService implements BrandAccessUseCase {

    private final ShopBrandRepository brandRepository;
    private final ShopBrandMemberRepository brandMemberRepository;
    private final ShopAdminMemberRepository memberRepository;

    public BrandAccessService(ShopBrandRepository brandRepository,
                              ShopBrandMemberRepository brandMemberRepository,
                              ShopAdminMemberRepository memberRepository) {
        this.brandRepository = brandRepository;
        this.brandMemberRepository = brandMemberRepository;
        this.memberRepository = memberRepository;
    }

    @Override
    public void requireAccess(Long adminId, ShopRole role, Long companyId, Long brandId) {
        ShopBrand brand = brandRepository.findById(brandId)
            .orElseThrow(() -> new ShopBusinessException(ShopErrorCode.BRAND_NOT_FOUND));
        Set<Long> assignedBrandIds = role == ShopRole.BRAND_ADMIN ? assignedBrandIds(adminId) : Set.of();
        if (!ShopAccessPolicy.canAccessBrand(role, companyId, assignedBrandIds, brand)) {
            throw new ShopBusinessException(ShopErrorCode.BRAND_ACCESS_DENIED);
        }
    }

    private Set<Long> assignedBrandIds(Long adminId) {
        return memberRepository.findByAdminId(adminId)
            .map(m -> brandMemberRepository.findBrandIdsByMemberId(m.getId()))
            .orElse(Set.of());
    }
}
