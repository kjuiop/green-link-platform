package com.greenlink.shopcore.application.auth;

import com.greenlink.shopcore.application.auth.dto.ShopSession;
import com.greenlink.shopcore.application.auth.dto.VerifiedIdentity;
import com.greenlink.shopcore.application.auth.port.IdentityVerifier;
import com.greenlink.shopcore.application.auth.service.RefreshTokenService;
import com.greenlink.shopcore.application.auth.service.ShopLoginService;
import com.greenlink.shopcore.application.auth.service.ShopSessionResolver;
import com.greenlink.shopcore.domain.auth.AccessStatus;
import com.greenlink.shopcore.domain.auth.ShopRole;
import com.greenlink.shopcore.global.exception.ShopBusinessException;
import com.greenlink.shopcore.global.exception.ShopErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class ShopLoginAndRefreshServiceTest {

    @Mock IdentityVerifier identityVerifier;
    @Mock ShopSessionResolver sessionResolver;

    private static final VerifiedIdentity IDENTITY = new VerifiedIdentity(10L, "login", "이름", 7L, "회사", true, false, true, false);
    private static final ShopSession SESSION =
        new ShopSession("tok", "login", "이름", 7L, "회사", ShopRole.PARTNER_ADMIN, AccessStatus.APPROVED);

    @Test
    void 로그인은_온우림_검증_결과로_세션을_만든다() {
        given(identityVerifier.verify("login", "pw")).willReturn(IDENTITY);
        given(sessionResolver.resolve(IDENTITY)).willReturn(SESSION);

        assertThat(new ShopLoginService(identityVerifier, sessionResolver).login("login", "pw")).isEqualTo(SESSION);
    }

    @Test
    void 로그인_검증_실패는_세션_생성_없이_전파된다() {
        given(identityVerifier.verify("login", "bad"))
            .willThrow(new ShopBusinessException(ShopErrorCode.AUTH_INVALID_CREDENTIALS));

        assertThatThrownBy(() -> new ShopLoginService(identityVerifier, sessionResolver).login("login", "bad"))
            .isInstanceOfSatisfying(ShopBusinessException.class,
                e -> assertThat(e.getErrorCode()).isEqualTo(ShopErrorCode.AUTH_INVALID_CREDENTIALS));
        verifyNoInteractions(sessionResolver);
    }

    @Test
    void 갱신은_온우림_계정을_재확인한_뒤_세션을_다시_만든다() {
        given(identityVerifier.getAccount(10L)).willReturn(IDENTITY);
        given(sessionResolver.resolve(IDENTITY)).willReturn(SESSION);

        assertThat(new RefreshTokenService(identityVerifier, sessionResolver).refresh(10L)).isEqualTo(SESSION);
    }

    @Test
    void 비활성_계정은_갱신이_차단된다() {
        given(identityVerifier.getAccount(10L))
            .willThrow(new ShopBusinessException(ShopErrorCode.AUTH_ACCOUNT_INACTIVE));

        assertThatThrownBy(() -> new RefreshTokenService(identityVerifier, sessionResolver).refresh(10L))
            .isInstanceOfSatisfying(ShopBusinessException.class,
                e -> assertThat(e.getErrorCode()).isEqualTo(ShopErrorCode.AUTH_ACCOUNT_INACTIVE));
        verifyNoInteractions(sessionResolver);
    }

    @Test
    void 온우림_서버_장애는_갱신에서_503으로_전파된다() {
        given(identityVerifier.getAccount(any()))
            .willThrow(new ShopBusinessException(ShopErrorCode.AUTH_UPSTREAM_UNAVAILABLE));

        assertThatThrownBy(() -> new RefreshTokenService(identityVerifier, sessionResolver).refresh(10L))
            .isInstanceOfSatisfying(ShopBusinessException.class,
                e -> assertThat(e.getErrorCode()).isEqualTo(ShopErrorCode.AUTH_UPSTREAM_UNAVAILABLE));
    }
}
