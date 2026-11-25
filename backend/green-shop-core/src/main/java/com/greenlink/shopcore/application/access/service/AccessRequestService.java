package com.greenlink.shopcore.application.access.service;

import com.greenlink.shopcore.application.access.dto.MyAccess;
import com.greenlink.shopcore.application.access.usecase.AccessRequestUseCase;
import com.greenlink.shopcore.application.auth.dto.VerifiedIdentity;
import com.greenlink.shopcore.application.auth.port.IdentityVerifier;
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
    private final Clock clock;

    public AccessRequestService(ShopAdminMemberRepository memberRepository,
                                IdentityVerifier identityVerifier,
                                Clock clock) {
        this.memberRepository = memberRepository;
        this.identityVerifier = identityVerifier;
        this.clock = clock;
    }

    @Override
    public MyAccess getMyAccess(Long adminId, boolean superAdmin) {
        if (superAdmin) {
            return new MyAccess(AccessStatus.APPROVED, null);
        }
        return memberRepository.findByAdminId(adminId)
            .map(m -> new MyAccess(AccessStatus.from(m.getStatus()), m.getRejectReason()))
            .orElse(new MyAccess(AccessStatus.NONE, null));
    }

    @Override
    public MyAccess apply(Long adminId, boolean superAdmin) {
        if (superAdmin) {
            throw new ShopBusinessException(ShopErrorCode.ACCESS_NOT_REQUIRED);
        }
        VerifiedIdentity identity = identityVerifier.getAccount(adminId);
        if (identity.superAdmin()) {
            throw new ShopBusinessException(ShopErrorCode.ACCESS_NOT_REQUIRED);
        }
        if (identity.companyId() == null) {
            throw new ShopBusinessException(ShopErrorCode.ACCESS_COMPANY_REQUIRED);
        }

        LocalDateTime now = LocalDateTime.now(clock);
        ShopAdminMember member = memberRepository.findByAdminId(adminId).orElse(null);
        if (member == null) {
            member = ShopAdminMember.request(adminId, identity.companyId(), ShopRole.PARTNER_ADMIN, now);
        } else {
            member.reapply(identity.companyId(), ShopRole.PARTNER_ADMIN, now);
        }
        ShopAdminMember saved = memberRepository.save(member);
        return new MyAccess(AccessStatus.from(saved.getStatus()), saved.getRejectReason());
    }
}
