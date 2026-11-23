package com.greenlink.shopcore.application.auth.service;

import com.greenlink.shopcore.application.auth.dto.ShopSession;
import com.greenlink.shopcore.application.auth.port.IdentityVerifier;
import com.greenlink.shopcore.application.auth.usecase.RefreshTokenUseCase;
import org.springframework.stereotype.Service;

@Service
public class RefreshTokenService implements RefreshTokenUseCase {

    private final IdentityVerifier identityVerifier;
    private final ShopSessionResolver sessionResolver;

    public RefreshTokenService(IdentityVerifier identityVerifier, ShopSessionResolver sessionResolver) {
        this.identityVerifier = identityVerifier;
        this.sessionResolver = sessionResolver;
    }

    @Override
    public ShopSession refresh(Long adminId) {
        return sessionResolver.resolve(identityVerifier.getAccount(adminId));
    }
}
