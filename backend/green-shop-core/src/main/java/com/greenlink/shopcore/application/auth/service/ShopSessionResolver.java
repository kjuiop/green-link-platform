package com.greenlink.shopcore.application.auth.service;

import com.greenlink.shopcore.application.auth.dto.ShopSession;
import com.greenlink.shopcore.application.auth.dto.VerifiedIdentity;
import com.greenlink.shopcore.application.auth.port.AccessTokenIssuer;
import com.greenlink.shopcore.domain.auth.AccessStatus;
import com.greenlink.shopcore.domain.auth.ShopRole;
import com.greenlink.shopcore.domain.auth.TokenScope;
import com.greenlink.shopcore.domain.member.model.ShopAdminMember;
import com.greenlink.shopcore.domain.member.model.ShopAdminStatus;
import com.greenlink.shopcore.domain.member.port.ShopAdminMemberRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

@Component
public class ShopSessionResolver {

    private static final Logger log = LoggerFactory.getLogger(ShopSessionResolver.class);

    private final ShopAdminMemberRepository memberRepository;
    private final AccessTokenIssuer tokenIssuer;

    public ShopSessionResolver(ShopAdminMemberRepository memberRepository, AccessTokenIssuer tokenIssuer) {
        this.memberRepository = memberRepository;
        this.tokenIssuer = tokenIssuer;
    }

    @Transactional(readOnly = true)
    public ShopSession resolve(VerifiedIdentity identity) {
        if (identity.superAdmin()) {
            return session(identity, ShopRole.SUPER_ADMIN, AccessStatus.APPROVED, TokenScope.FULL);
        }

        Optional<ShopAdminMember> member = memberRepository.findByAdminId(identity.adminId())
            .filter(m -> m.getStatus() != ShopAdminStatus.APPROVED || companyMatches(m, identity));

        AccessStatus status = member.map(m -> AccessStatus.from(m.getStatus())).orElse(AccessStatus.NONE);
        if (status == AccessStatus.APPROVED) {
            return session(identity, member.get().getRole(), status, TokenScope.FULL);
        }
        return session(identity, null, status, TokenScope.LIMITED);
    }

    private boolean companyMatches(ShopAdminMember member, VerifiedIdentity identity) {
        boolean matches = Objects.equals(member.getCompanyId(), identity.companyId());
        if (!matches) {
            log.warn("shop 멤버 회사 불일치로 접근을 부여하지 않음: adminId={}, memberCompanyId={}, identityCompanyId={}",
                identity.adminId(), member.getCompanyId(), identity.companyId());
        }
        return matches;
    }

    private ShopSession session(VerifiedIdentity identity, ShopRole role, AccessStatus status, TokenScope scope) {
        String token = tokenIssuer.issue(identity.adminId(), identity.companyId(), role, scope);
        return new ShopSession(token, identity.loginId(), identity.name(),
            identity.companyId(), identity.companyName(), role, status);
    }
}
