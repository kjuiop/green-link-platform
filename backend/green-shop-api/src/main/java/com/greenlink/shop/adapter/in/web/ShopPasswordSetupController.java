package com.greenlink.shop.adapter.in.web;

import com.greenlink.shop.adapter.in.web.dto.PasswordSetupRequest;
import com.greenlink.shopcore.application.auth.usecase.ShopPasswordSetupUseCase;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class ShopPasswordSetupController {

    private final ShopPasswordSetupUseCase passwordSetupUseCase;
    private final ClientIpResolver clientIp;

    public ShopPasswordSetupController(ShopPasswordSetupUseCase passwordSetupUseCase, ClientIpResolver clientIp) {
        this.passwordSetupUseCase = passwordSetupUseCase;
        this.clientIp = clientIp;
    }

    @PostMapping("/password-setup")
    public ResponseEntity<Void> setup(@Valid @RequestBody PasswordSetupRequest request, HttpServletRequest http) {
        passwordSetupUseCase.setup(request.loginId(), request.phone(), request.newPassword(), clientIp.resolve(http));
        return ResponseEntity.noContent().build();
    }
}
