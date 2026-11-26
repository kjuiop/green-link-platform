package com.greenlink.shopcore.application.auth.service;

import com.greenlink.shopcore.application.auth.dto.AccessDecision;
import com.greenlink.shopcore.application.auth.dto.VerifiedIdentity;
import com.greenlink.shopcore.domain.auth.AccessRequestType;
import com.greenlink.shopcore.domain.auth.AccessStatus;
import com.greenlink.shopcore.domain.auth.ShopRole;
import com.greenlink.shopcore.domain.auth.TokenScope;
import com.greenlink.shopcore.domain.member.model.ShopAdminMember;
import com.greenlink.shopcore.domain.member.model.ShopAdminStatus;
import com.greenlink.shopcore.domain.member.port.ShopAdminMemberRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Component
public class AccessEvaluator {

    private static final Logger log = LoggerFactory.getLogger(AccessEvaluator.class);

    private final ShopAdminMemberRepository memberRepository;

    public AccessEvaluator(ShopAdminMemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public AccessDecision evaluate(VerifiedIdentity identity) {
        if (identity.superAdmin()) {
            return new AccessDecision(AccessStatus.APPROVED, ShopRole.SUPER_ADMIN, TokenScope.FULL, null, null, null, Set.of());
        }

        AccessRequestType requestType = requestTypeOf(identity);
        Optional<ShopAdminMember> found = memberRepository.findByAdminId(identity.adminId())
            .filter(m -> m.getStatus() != ShopAdminStatus.REMOVED)
            .filter(m -> m.getStatus() != ShopAdminStatus.APPROVED || companyMatches(m, identity));
        if (found.isEmpty()) {
            return new AccessDecision(AccessStatus.NONE, null, TokenScope.LIMITED, null, requestType, null, Set.of());
        }

        ShopAdminMember member = found.get();
        if (member.getStatus() == ShopAdminStatus.APPROVED) {
            if (!identity.companySiteEnabled()) {
                return new AccessDecision(AccessStatus.SITE_DISABLED, null, TokenScope.LIMITED, null,
                    requestType, member.getRequestedRole(), member.getRequestedBrandIds());
            }
            return new AccessDecision(AccessStatus.APPROVED, member.getRole(), TokenScope.FULL, null,
                requestType, member.getRequestedRole(), member.getRequestedBrandIds());
        }
        return new AccessDecision(AccessStatus.from(member.getStatus()), null, TokenScope.LIMITED,
            member.getRejectReason(), requestType, member.getRequestedRole(), member.getRequestedBrandIds());
    }

    public AccessRequestType requestTypeOf(VerifiedIdentity identity) {
        return identity.companyId() != null && !identity.companySiteEnabled()
            ? AccessRequestType.COMPANY
            : AccessRequestType.JOIN;
    }

    private boolean companyMatches(ShopAdminMember member, VerifiedIdentity identity) {
        boolean matches = Objects.equals(member.getCompanyId(), identity.companyId());
        if (!matches) {
            log.warn("shop 멤버 회사 불일치로 접근을 부여하지 않음: adminId={}, memberCompanyId={}, identityCompanyId={}",
                identity.adminId(), member.getCompanyId(), identity.companyId());
        }
        return matches;
    }
}
