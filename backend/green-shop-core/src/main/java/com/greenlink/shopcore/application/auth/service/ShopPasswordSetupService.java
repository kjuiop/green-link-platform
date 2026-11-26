package com.greenlink.shopcore.application.auth.service;

import com.greenlink.shopcore.application.auth.port.IdentityRegistrar;
import com.greenlink.shopcore.application.auth.port.SignupRateLimiter;
import com.greenlink.shopcore.application.auth.port.SignupRateLimiter.Bucket;
import com.greenlink.shopcore.application.auth.usecase.ShopPasswordSetupUseCase;
import com.greenlink.shopcore.global.exception.ShopBusinessException;
import com.greenlink.shopcore.global.exception.ShopErrorCode;
import org.springframework.stereotype.Service;

@Service
public class ShopPasswordSetupService implements ShopPasswordSetupUseCase {

    private final IdentityRegistrar registrar;
    private final SignupRateLimiter rateLimiter;

    public ShopPasswordSetupService(IdentityRegistrar registrar, SignupRateLimiter rateLimiter) {
        this.registrar = registrar;
        this.rateLimiter = rateLimiter;
    }

    @Override
    public void setup(String loginId, String phone, String newPassword, String clientKey) {
        if (!rateLimiter.tryAcquire(Bucket.PASSWORD_SETUP, clientKey)) {
            throw new ShopBusinessException(ShopErrorCode.SIGNUP_RATE_LIMITED);
        }
        try {
            registrar.completePasswordSetup(loginId, phone, newPassword);
        } catch (ShopBusinessException e) {
            if (e.getErrorCode() == ShopErrorCode.AUTH_UPSTREAM_UNAVAILABLE) {
                throw e;
            }
            throw new ShopBusinessException(ShopErrorCode.AUTH_PASSWORD_SETUP_FAILED);
        }
    }
}
