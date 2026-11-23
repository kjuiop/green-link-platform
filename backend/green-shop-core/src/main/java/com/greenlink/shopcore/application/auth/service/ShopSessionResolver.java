package com.greenlink.shopcore.application.auth.service;

import com.greenlink.shopcore.application.auth.dto.ShopSession;
import com.greenlink.shopcore.application.auth.dto.VerifiedIdentity;
import com.greenlink.shopcore.application.auth.port.AccessTokenIssuer;
import com.greenlink.shopcore.domain.auth.AccessStatus;
import com.greenlink.shopcore.domain.auth.ShopRole;
import com.greenlink.shopcore.domain.auth.TokenScope;
import com.greenlink.shopcore.domain.member.model.ShopAdminMember;
import com.greenlink.shopcore.domain.member.port.ShopAdminMemberRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
public class ShopSessionResolver {

    private final ShopAdminMemberRepository memberRepository;
    private final AccessTokenIssuer tokenIssuer;

    public ShopSessionResolver(ShopAdminMemberRepository memberRepository, AccessTokenIssuer tokenIssuer) {
        this.memberRepository = memberRepository;
        this.tokenIssuer = tokenIssuer;
    }

    @Transactional
    public ShopSession resolve(VerifiedIdentity identity) {
        if (identity.superAdmin()) {
            return session(identity, ShopRole.SUPER_ADMIN, AccessStatus.APPROVED, TokenScope.FULL);
        }

        Optional<ShopAdminMember> member = memberRepository.findByOnurimAdminId(identity.adminId());
        member.ifPresent(m -> {
            m.refreshSnapshot(identity.loginId(), identity.name(), identity.companyName());
            memberRepository.save(m);
        });

        AccessStatus status = member.map(m -> AccessStatus.from(m.getStatus())).orElse(AccessStatus.NONE);
        TokenScope scope = status == AccessStatus.APPROVED ? TokenScope.FULL : TokenScope.LIMITED;
        return session(identity, ShopRole.SHOP_ADMIN, status, scope);
    }

    private ShopSession session(VerifiedIdentity identity, ShopRole role, AccessStatus status, TokenScope scope) {
        ShopRole tokenRole = scope == TokenScope.FULL ? role : null;
        String token = tokenIssuer.issue(identity.adminId(), tokenRole, scope);
        return new ShopSession(token, identity.loginId(), identity.name(), identity.companyName(), role, status);
    }
}
