package com.greenlink.shopcore.application.auth;

import com.greenlink.shopcore.application.auth.port.IdentityRegistrar;
import com.greenlink.shopcore.application.auth.port.SignupRateLimiter;
import com.greenlink.shopcore.application.auth.port.SignupRateLimiter.Bucket;
import com.greenlink.shopcore.application.auth.service.ShopPasswordSetupService;
import com.greenlink.shopcore.global.exception.ErrorCode;
import com.greenlink.shopcore.global.exception.ShopBusinessException;
import com.greenlink.shopcore.global.exception.ShopErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class ShopPasswordSetupServiceTest {

    private static final String IP = "1.2.3.4";

    @Mock IdentityRegistrar registrar;
    @Mock SignupRateLimiter rateLimiter;

    private ShopPasswordSetupService service;

    @BeforeEach
    void setUp() {
        service = new ShopPasswordSetupService(registrar, rateLimiter);
    }

    private void assertCode(Runnable call, ErrorCode expected) {
        assertThatThrownBy(call::run).isInstanceOfSatisfying(ShopBusinessException.class,
            e -> assertThat(e.getErrorCode()).isEqualTo(expected));
    }

    @Test
    void 허용되면_온우림에_설정을_위임한다() {
        given(rateLimiter.tryAcquire(Bucket.PASSWORD_SETUP, IP)).willReturn(true);

        service.setup("brand01", "010-1234-5678", "Passw0rd!", IP);

        verify(registrar).completePasswordSetup("brand01", "010-1234-5678", "Passw0rd!");
    }

    @Test
    void 요청_한도를_넘으면_온우림을_호출하지_않는다() {
        given(rateLimiter.tryAcquire(Bucket.PASSWORD_SETUP, IP)).willReturn(false);

        assertCode(() -> service.setup("brand01", "010-1234-5678", "Passw0rd!", IP), ShopErrorCode.SIGNUP_RATE_LIMITED);
        verifyNoInteractions(registrar);
    }

    @Test
    void 업스트림이_어떤_사유로_거부해도_단일_에러로_통일한다() {
        given(rateLimiter.tryAcquire(Bucket.PASSWORD_SETUP, IP)).willReturn(true);
        doThrow(new ShopBusinessException(ShopErrorCode.AUTH_INVALID_CREDENTIALS))
            .when(registrar).completePasswordSetup("brand01", "010-0000-0000", "Passw0rd!");

        assertCode(() -> service.setup("brand01", "010-0000-0000", "Passw0rd!", IP),
            ShopErrorCode.AUTH_PASSWORD_SETUP_FAILED);
    }

    @Test
    void 업스트림_장애는_그대로_전달한다() {
        given(rateLimiter.tryAcquire(Bucket.PASSWORD_SETUP, IP)).willReturn(true);
        doThrow(new ShopBusinessException(ShopErrorCode.AUTH_UPSTREAM_UNAVAILABLE))
            .when(registrar).completePasswordSetup("brand01", "010-1234-5678", "Passw0rd!");

        assertCode(() -> service.setup("brand01", "010-1234-5678", "Passw0rd!", IP),
            ShopErrorCode.AUTH_UPSTREAM_UNAVAILABLE);
    }
}
