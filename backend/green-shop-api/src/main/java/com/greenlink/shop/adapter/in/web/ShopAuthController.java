package com.greenlink.shop.adapter.in.web;

import com.greenlink.shop.adapter.in.web.dto.LoginRequest;
import com.greenlink.shop.adapter.in.web.dto.SessionResponse;
import com.greenlink.shop.security.ShopPrincipal;
import com.greenlink.shopcore.application.auth.usecase.RefreshTokenUseCase;
import com.greenlink.shopcore.application.auth.usecase.ShopLoginUseCase;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class ShopAuthController {

    private final ShopLoginUseCase loginUseCase;
    private final RefreshTokenUseCase refreshUseCase;

    public ShopAuthController(ShopLoginUseCase loginUseCase, RefreshTokenUseCase refreshUseCase) {
        this.loginUseCase = loginUseCase;
        this.refreshUseCase = refreshUseCase;
    }

    @PostMapping("/login")
    public SessionResponse login(@Valid @RequestBody LoginRequest request) {
        return SessionResponse.from(loginUseCase.login(request.loginId(), request.password()));
    }

    @PostMapping("/refresh")
    public SessionResponse refresh(@AuthenticationPrincipal ShopPrincipal principal) {
        return SessionResponse.from(refreshUseCase.refresh(principal.adminId()));
    }
}
