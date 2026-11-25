package com.greenlink.shop.adapter.in.web;

import com.greenlink.shop.adapter.in.web.dto.MyAccessResponse;
import com.greenlink.shop.security.ShopPrincipal;
import com.greenlink.shopcore.application.access.usecase.AccessRequestUseCase;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AccessRequestController {

    private final AccessRequestUseCase accessRequestUseCase;

    public AccessRequestController(AccessRequestUseCase accessRequestUseCase) {
        this.accessRequestUseCase = accessRequestUseCase;
    }

    @GetMapping("/api/me")
    public MyAccessResponse me(@AuthenticationPrincipal ShopPrincipal principal) {
        return MyAccessResponse.from(accessRequestUseCase.getMyAccess(principal.adminId(), principal.companyId(), principal.superAdmin()));
    }

    @PostMapping("/api/access-requests")
    public MyAccessResponse apply(@AuthenticationPrincipal ShopPrincipal principal) {
        return MyAccessResponse.from(accessRequestUseCase.apply(principal.adminId(), principal.superAdmin()));
    }
}
