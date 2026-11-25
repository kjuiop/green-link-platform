package com.greenlink.shop.adapter.in.web;

import com.greenlink.shop.adapter.in.web.dto.ApproveRequest;
import com.greenlink.shop.adapter.in.web.dto.BrandAssignRequest;
import com.greenlink.shop.adapter.in.web.dto.RejectRequest;
import com.greenlink.shop.adapter.in.web.dto.RoleChangeRequest;
import com.greenlink.shop.adapter.in.web.dto.ShopAdminResponse;
import com.greenlink.shop.security.ShopPrincipal;
import com.greenlink.shopcore.application.admin.usecase.ShopAdminManagementUseCase;
import com.greenlink.shopcore.domain.member.model.ShopAdminStatus;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/members")
public class MemberController {

    private final ShopAdminManagementUseCase managementUseCase;

    public MemberController(ShopAdminManagementUseCase managementUseCase) {
        this.managementUseCase = managementUseCase;
    }

    @GetMapping
    public List<ShopAdminResponse> list(@RequestParam(required = false) ShopAdminStatus status,
                                        @AuthenticationPrincipal ShopPrincipal principal) {
        return managementUseCase.list(principal.toActor(), status).stream().map(ShopAdminResponse::from).toList();
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<Void> approve(@PathVariable Long id,
                                        @RequestBody(required = false) ApproveRequest request,
                                        @AuthenticationPrincipal ShopPrincipal principal) {
        managementUseCase.approve(principal.toActor(), id,
            request == null ? null : request.role(), request == null ? null : request.brandIds());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<Void> reject(@PathVariable Long id, @Valid @RequestBody RejectRequest request,
                                       @AuthenticationPrincipal ShopPrincipal principal) {
        managementUseCase.reject(principal.toActor(), id, request.reason());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/role")
    public ResponseEntity<Void> changeRole(@PathVariable Long id, @Valid @RequestBody RoleChangeRequest request,
                                           @AuthenticationPrincipal ShopPrincipal principal) {
        managementUseCase.changeRole(principal.toActor(), id, request.role());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/brands")
    public ResponseEntity<Void> assignBrands(@PathVariable Long id, @Valid @RequestBody BrandAssignRequest request,
                                             @AuthenticationPrincipal ShopPrincipal principal) {
        managementUseCase.assignBrands(principal.toActor(), id, request.brandIds());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remove(@PathVariable Long id, @AuthenticationPrincipal ShopPrincipal principal) {
        managementUseCase.remove(principal.toActor(), id);
        return ResponseEntity.noContent().build();
    }
}
