package com.greenlink.shopcore.application.auth.service;

import com.greenlink.shopcore.application.auth.dto.AccessDecision;
import com.greenlink.shopcore.application.auth.dto.ShopSession;
import com.greenlink.shopcore.application.auth.dto.VerifiedIdentity;
import com.greenlink.shopcore.application.auth.port.AccessTokenIssuer;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ShopSessionResolver {

    private final AccessEvaluator accessEvaluator;
    private final AccessTokenIssuer tokenIssuer;

    public ShopSessionResolver(AccessEvaluator accessEvaluator, AccessTokenIssuer tokenIssuer) {
        this.accessEvaluator = accessEvaluator;
        this.tokenIssuer = tokenIssuer;
    }

    @Transactional(readOnly = true)
    public ShopSession resolve(VerifiedIdentity identity) {
        AccessDecision d = accessEvaluator.evaluate(identity);
        String token = tokenIssuer.issue(identity.adminId(), identity.companyId(), d.role(), d.scope());
        return new ShopSession(token, identity.loginId(), identity.name(),
            identity.companyId(), identity.companyName(), d.role(), d.status());
    }
}
