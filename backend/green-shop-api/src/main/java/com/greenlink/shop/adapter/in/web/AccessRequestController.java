package com.greenlink.shop.adapter.in.web;

import com.greenlink.shop.adapter.in.web.dto.AccessApplyRequest;
import com.greenlink.shop.adapter.in.web.dto.ApplicableBrandResponse;
import com.greenlink.shop.adapter.in.web.dto.MyAccessResponse;
import com.greenlink.shop.security.ShopPrincipal;
import com.greenlink.shopcore.application.access.usecase.AccessRequestUseCase;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class AccessRequestController {

    private final AccessRequestUseCase accessRequestUseCase;

    public AccessRequestController(AccessRequestUseCase accessRequestUseCase) {
        this.accessRequestUseCase = accessRequestUseCase;
    }

    @GetMapping("/api/me")
    public MyAccessResponse me(@AuthenticationPrincipal ShopPrincipal principal) {
        return MyAccessResponse.from(accessRequestUseCase.getMyAccess(principal.adminId(), principal.superAdmin()));
    }

    @PostMapping("/api/access-requests")
    public MyAccessResponse apply(@AuthenticationPrincipal ShopPrincipal principal,
                                  @RequestBody(required = false) AccessApplyRequest request) {
        return MyAccessResponse.from(accessRequestUseCase.apply(principal.adminId(), principal.superAdmin(),
            request == null ? null : request.requestedRole(),
            request == null ? null : request.requestedBrandIds()));
    }

    @GetMapping("/api/access-requests/brands")
    public List<ApplicableBrandResponse> applicableBrands(@AuthenticationPrincipal ShopPrincipal principal) {
        return accessRequestUseCase.getApplicableBrands(principal.adminId(), principal.superAdmin()).stream()
            .map(ApplicableBrandResponse::from)
            .toList();
    }
}
