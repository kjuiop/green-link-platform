package com.greenlink.shop.adapter.in.web;

import com.greenlink.shop.adapter.in.web.dto.BrandRequest;
import com.greenlink.shop.adapter.in.web.dto.BrandResponse;
import com.greenlink.shop.security.ShopPrincipal;
import com.greenlink.shopcore.application.brand.usecase.BrandManagementUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/brands")
public class BrandController {

    private final BrandManagementUseCase brandUseCase;

    public BrandController(BrandManagementUseCase brandUseCase) {
        this.brandUseCase = brandUseCase;
    }

    @GetMapping
    public List<BrandResponse> list(@RequestParam(required = false) Long companyId,
                                    @AuthenticationPrincipal ShopPrincipal principal) {
        return brandUseCase.list(principal.toActor(), companyId).stream().map(BrandResponse::from).toList();
    }

    @PostMapping
    public ResponseEntity<BrandResponse> create(@Valid @RequestBody BrandRequest request,
                                                @AuthenticationPrincipal ShopPrincipal principal) {
        return ResponseEntity.status(HttpStatus.CREATED).body(BrandResponse.from(
            brandUseCase.create(principal.toActor(), request.companyId(), request.name())));
    }

    @PatchMapping("/{id}")
    public BrandResponse rename(@PathVariable Long id, @Valid @RequestBody BrandRequest request,
                                @AuthenticationPrincipal ShopPrincipal principal) {
        return BrandResponse.from(brandUseCase.rename(principal.toActor(), id, request.name()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, @AuthenticationPrincipal ShopPrincipal principal) {
        brandUseCase.delete(principal.toActor(), id);
        return ResponseEntity.noContent().build();
    }
}
