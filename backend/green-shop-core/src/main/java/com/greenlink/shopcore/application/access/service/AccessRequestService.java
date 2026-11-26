package com.greenlink.shopcore.application.access.service;

import com.greenlink.shopcore.application.access.dto.ApplicableBrand;
import com.greenlink.shopcore.application.access.dto.MyAccess;
import com.greenlink.shopcore.application.access.usecase.AccessRequestUseCase;
import com.greenlink.shopcore.application.auth.dto.AccessDecision;
import com.greenlink.shopcore.application.auth.dto.VerifiedIdentity;
import com.greenlink.shopcore.application.auth.port.IdentityVerifier;
import com.greenlink.shopcore.application.auth.service.AccessEvaluator;
import com.greenlink.shopcore.domain.auth.AccessRequestType;
import com.greenlink.shopcore.domain.auth.AccessStatus;
import com.greenlink.shopcore.domain.auth.ShopRole;
import com.greenlink.shopcore.domain.brand.model.ShopBrand;
import com.greenlink.shopcore.domain.brand.port.ShopBrandRepository;
import com.greenlink.shopcore.domain.member.model.ShopAdminMember;
import com.greenlink.shopcore.domain.member.port.ShopAdminMemberRepository;
import com.greenlink.shopcore.global.exception.ShopBusinessException;
import com.greenlink.shopcore.global.exception.ShopErrorCode;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AccessRequestService implements AccessRequestUseCase {

    private final ShopAdminMemberRepository memberRepository;
    private final ShopBrandRepository brandRepository;
    private final IdentityVerifier identityVerifier;
    private final AccessEvaluator accessEvaluator;
    private final Clock clock;

    public AccessRequestService(ShopAdminMemberRepository memberRepository,
                                ShopBrandRepository brandRepository,
                                IdentityVerifier identityVerifier,
                                AccessEvaluator accessEvaluator,
                                Clock clock) {
        this.memberRepository = memberRepository;
        this.brandRepository = brandRepository;
        this.identityVerifier = identityVerifier;
        this.accessEvaluator = accessEvaluator;
        this.clock = clock;
    }

    @Override
    public MyAccess getMyAccess(Long adminId, boolean superAdmin) {
        if (superAdmin) {
            return new MyAccess(AccessStatus.APPROVED, null, null, null, Set.of());
        }
        AccessDecision d = accessEvaluator.evaluate(identityVerifier.getAccount(adminId));
        return new MyAccess(d.status(), d.rejectReason(), d.requestType(), d.requestedRole(), d.requestedBrandIds());
    }

    @Override
    public MyAccess apply(Long adminId, boolean superAdmin, ShopRole requestedRole, Set<Long> requestedBrandIds) {
        if (superAdmin) {
            throw new ShopBusinessException(ShopErrorCode.ACCESS_NOT_REQUIRED);
        }
        VerifiedIdentity identity = identityVerifier.getAccount(adminId);
        if (identity.superAdmin()) {
            throw new ShopBusinessException(ShopErrorCode.ACCESS_NOT_REQUIRED);
        }

        ShopAdminMember member = memberRepository.findByAdminId(adminId).orElse(null);
        Long companyId = identity.companyId() != null ? identity.companyId()
            : member != null ? member.getCompanyId() : null;
        if (companyId == null) {
            throw new ShopBusinessException(ShopErrorCode.ACCESS_COMPANY_REQUIRED);
        }

        AccessRequestType requestType = accessEvaluator.requestTypeOf(identity);
        ShopRole role = requestType == AccessRequestType.COMPANY
            ? ShopRole.PARTNER_ADMIN
            : requestedRole != null ? requestedRole : member != null ? member.getRequestedRole() : null;

        Set<Long> brandIds = resolveBrands(companyId, role, requestedBrandIds);

        LocalDateTime now = LocalDateTime.now(clock);
        if (member == null) {
            member = ShopAdminMember.request(adminId, companyId, role, brandIds, now);
        } else {
            member.reapply(companyId, role, brandIds, now);
        }
        ShopAdminMember saved = memberRepository.save(member);
        return new MyAccess(AccessStatus.from(saved.getStatus()), saved.getRejectReason(),
            requestType, saved.getRequestedRole(), saved.getRequestedBrandIds());
    }

    @Override
    public List<ApplicableBrand> getApplicableBrands(Long adminId, boolean superAdmin) {
        if (superAdmin) {
            return List.of();
        }
        VerifiedIdentity identity = identityVerifier.getAccount(adminId);
        Long companyId = identity.companyId() != null ? identity.companyId()
            : memberRepository.findByAdminId(adminId).map(ShopAdminMember::getCompanyId).orElse(null);
        if (identity.superAdmin() || companyId == null) {
            return List.of();
        }
        return brandRepository.findAllByCompanyId(companyId).stream()
            .map(b -> new ApplicableBrand(b.getId(), b.getName()))
            .toList();
    }

    private Set<Long> resolveBrands(Long companyId, ShopRole role, Set<Long> requested) {
        if (role != ShopRole.BRAND_ADMIN) {
            return Set.of();
        }
        Set<Long> companyBrandIds = brandRepository.findAllByCompanyId(companyId).stream()
            .map(ShopBrand::getId)
            .collect(Collectors.toSet());
        if (companyBrandIds.isEmpty()) {
            return Set.of();
        }
        Set<Long> ids = requested == null ? Set.of() : requested;
        if (ids.isEmpty()) {
            throw new ShopBusinessException(ShopErrorCode.MEMBER_BRAND_REQUIRED);
        }
        if (!companyBrandIds.containsAll(ids)) {
            throw new ShopBusinessException(ShopErrorCode.MEMBER_BRAND_INVALID);
        }
        return ids;
    }
}
