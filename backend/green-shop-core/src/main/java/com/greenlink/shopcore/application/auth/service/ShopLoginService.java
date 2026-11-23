package com.greenlink.shopcore.application.auth.service;

import com.greenlink.shopcore.application.auth.dto.ShopSession;
import com.greenlink.shopcore.application.auth.port.IdentityVerifier;
import com.greenlink.shopcore.application.auth.usecase.ShopLoginUseCase;
import org.springframework.stereotype.Service;

@Service
public class ShopLoginService implements ShopLoginUseCase {

    private final IdentityVerifier identityVerifier;
    private final ShopSessionResolver sessionResolver;

    public ShopLoginService(IdentityVerifier identityVerifier, ShopSessionResolver sessionResolver) {
        this.identityVerifier = identityVerifier;
        this.sessionResolver = sessionResolver;
    }

    @Override
    public ShopSession login(String loginId, String password) {
        return sessionResolver.resolve(identityVerifier.verify(loginId, password));
    }
}
