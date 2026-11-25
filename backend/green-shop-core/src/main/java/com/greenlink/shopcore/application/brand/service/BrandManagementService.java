package com.greenlink.shopcore.application.brand.service;

import com.greenlink.shopcore.application.brand.usecase.BrandManagementUseCase;
import com.greenlink.shopcore.application.common.ManagementActor;
import com.greenlink.shopcore.application.common.ManagementGuard;
import com.greenlink.shopcore.domain.auth.ShopRole;
import com.greenlink.shopcore.domain.brand.model.ShopBrand;
import com.greenlink.shopcore.domain.brand.port.ShopBrandMemberRepository;
import com.greenlink.shopcore.domain.brand.port.ShopBrandRepository;
import com.greenlink.shopcore.domain.member.model.ShopAdminMember;
import com.greenlink.shopcore.domain.member.port.ShopAdminMemberRepository;
import com.greenlink.shopcore.global.exception.ShopBusinessException;
import com.greenlink.shopcore.global.exception.ShopErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class BrandManagementService implements BrandManagementUseCase {

    private final ShopBrandRepository brandRepository;
    private final ShopBrandMemberRepository brandMemberRepository;
    private final ShopAdminMemberRepository memberRepository;
    private final ManagementGuard guard;
    private final Clock clock;

    public BrandManagementService(ShopBrandRepository brandRepository,
                                  ShopBrandMemberRepository brandMemberRepository,
                                  ShopAdminMemberRepository memberRepository,
                                  ManagementGuard guard,
                                  Clock clock) {
        this.brandRepository = brandRepository;
        this.brandMemberRepository = brandMemberRepository;
        this.memberRepository = memberRepository;
        this.guard = guard;
        this.clock = clock;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShopBrand> list(ManagementActor actor, Long companyId) {
        if (actor.role() == ShopRole.BRAND_ADMIN) {
            return listAssigned(actor);
        }
        Long verifiedCompanyId = guard.requireManager(actor);
        if (actor.role() == ShopRole.PARTNER_ADMIN) {
            return brandRepository.findAllByCompanyId(verifiedCompanyId);
        }
        return companyId == null ? brandRepository.findAll() : brandRepository.findAllByCompanyId(companyId);
    }

    @Override
    public ShopBrand create(ManagementActor actor, Long companyId, String name) {
        Long verifiedCompanyId = guard.requireManager(actor);
        Long targetCompanyId = actor.role() == ShopRole.PARTNER_ADMIN ? verifiedCompanyId : companyId;
        if (targetCompanyId == null) {
            throw new ShopBusinessException(ShopErrorCode.BRAND_COMPANY_REQUIRED);
        }
        ShopBrand brand = ShopBrand.create(targetCompanyId, name);
        requireNameAvailable(targetCompanyId, brand.getName());
        return brandRepository.save(brand);
    }

    @Override
    public ShopBrand rename(ManagementActor actor, Long brandId, String name) {
        ShopBrand brand = find(brandId);
        guard.requireCanManage(actor, brand.getCompanyId());
        String normalized = ShopBrand.normalize(name);
        if (!normalized.equals(brand.getName())) {
            requireNameAvailable(brand.getCompanyId(), normalized);
            brand.rename(normalized);
        }
        return brandRepository.save(brand);
    }

    @Override
    public void delete(ManagementActor actor, Long brandId) {
        ShopBrand brand = find(brandId);
        guard.requireCanManage(actor, brand.getCompanyId());
        LocalDateTime now = LocalDateTime.now(clock);
        brand.delete(now);
        brandRepository.save(brand);
        brandMemberRepository.unassignAllByBrandId(brandId, now);
    }

    private List<ShopBrand> listAssigned(ManagementActor actor) {
        ShopAdminMember member = memberRepository.findByAdminId(actor.adminId())
            .filter(m -> m.isApprovedAs(ShopRole.BRAND_ADMIN))
            .orElseThrow(() -> new ShopBusinessException(ShopErrorCode.ACCESS_MANAGE_FORBIDDEN));
        Set<Long> assigned = brandMemberRepository.findBrandIdsByMemberId(member.getId());
        return brandRepository.findAllByCompanyId(member.getCompanyId()).stream()
            .filter(b -> assigned.contains(b.getId()))
            .toList();
    }

    private void requireNameAvailable(Long companyId, String name) {
        if (brandRepository.existsByCompanyIdAndName(companyId, name)) {
            throw new ShopBusinessException(ShopErrorCode.BRAND_NAME_DUPLICATED);
        }
    }

    private ShopBrand find(Long brandId) {
        return brandRepository.findById(brandId)
            .orElseThrow(() -> new ShopBusinessException(ShopErrorCode.BRAND_NOT_FOUND));
    }
}
