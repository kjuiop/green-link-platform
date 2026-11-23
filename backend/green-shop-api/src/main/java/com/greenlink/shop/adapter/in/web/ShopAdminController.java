package com.greenlink.shop.adapter.in.web;

import com.greenlink.shop.adapter.in.web.dto.RejectRequest;
import com.greenlink.shop.adapter.in.web.dto.ShopAdminResponse;
import com.greenlink.shop.security.ShopPrincipal;
import com.greenlink.shopcore.application.admin.usecase.ShopAdminManagementUseCase;
import com.greenlink.shopcore.domain.member.model.ShopAdminStatus;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/shop-admins")
public class ShopAdminController {

    private final ShopAdminManagementUseCase managementUseCase;

    public ShopAdminController(ShopAdminManagementUseCase managementUseCase) {
        this.managementUseCase = managementUseCase;
    }

    @GetMapping
    public List<ShopAdminResponse> list(@RequestParam(required = false) ShopAdminStatus status) {
        return managementUseCase.list(status).stream().map(ShopAdminResponse::from).toList();
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<Void> approve(@PathVariable Long id, @AuthenticationPrincipal ShopPrincipal principal) {
        managementUseCase.approve(id, principal.adminId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<Void> reject(@PathVariable Long id, @Valid @RequestBody RejectRequest request,
                                       @AuthenticationPrincipal ShopPrincipal principal) {
        managementUseCase.reject(id, request.reason(), principal.adminId());
        return ResponseEntity.noContent().build();
    }
}
