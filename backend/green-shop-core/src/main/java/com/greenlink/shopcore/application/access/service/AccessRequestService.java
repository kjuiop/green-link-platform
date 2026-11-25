package com.greenlink.shopcore.application.access.service;

import com.greenlink.shopcore.application.access.dto.MyAccess;
import com.greenlink.shopcore.application.access.usecase.AccessRequestUseCase;
import com.greenlink.shopcore.application.auth.dto.AccessDecision;
import com.greenlink.shopcore.application.auth.dto.VerifiedIdentity;
import com.greenlink.shopcore.application.auth.port.IdentityVerifier;
import com.greenlink.shopcore.application.auth.service.AccessEvaluator;
import com.greenlink.shopcore.domain.auth.AccessRequestType;
import com.greenlink.shopcore.domain.auth.AccessStatus;
import com.greenlink.shopcore.domain.auth.ShopRole;
import com.greenlink.shopcore.domain.member.model.ShopAdminMember;
import com.greenlink.shopcore.domain.member.port.ShopAdminMemberRepository;
import com.greenlink.shopcore.global.exception.ShopBusinessException;
import com.greenlink.shopcore.global.exception.ShopErrorCode;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;

@Service
public class AccessRequestService implements AccessRequestUseCase {

    private final ShopAdminMemberRepository memberRepository;
    private final IdentityVerifier identityVerifier;
    private final AccessEvaluator accessEvaluator;
    private final Clock clock;

    public AccessRequestService(ShopAdminMemberRepository memberRepository,
                                IdentityVerifier identityVerifier,
                                AccessEvaluator accessEvaluator,
                                Clock clock) {
        this.memberRepository = memberRepository;
        this.identityVerifier = identityVerifier;
        this.accessEvaluator = accessEvaluator;
        this.clock = clock;
    }

    @Override
    public MyAccess getMyAccess(Long adminId, boolean superAdmin) {
        if (superAdmin) {
            return new MyAccess(AccessStatus.APPROVED, null, null, null);
        }
        AccessDecision d = accessEvaluator.evaluate(identityVerifier.getAccount(adminId));
        return new MyAccess(d.status(), d.rejectReason(), d.requestType(), d.requestedRole());
    }

    @Override
    public MyAccess apply(Long adminId, boolean superAdmin, ShopRole requestedRole) {
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

        LocalDateTime now = LocalDateTime.now(clock);
        if (member == null) {
            member = ShopAdminMember.request(adminId, companyId, role, now);
        } else {
            member.reapply(companyId, role, now);
        }
        ShopAdminMember saved = memberRepository.save(member);
        return new MyAccess(AccessStatus.from(saved.getStatus()), saved.getRejectReason(),
            requestType, saved.getRequestedRole());
    }
}
