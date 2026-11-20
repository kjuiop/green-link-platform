package com.greenlink.admin.adapter.in.web;

import com.greenlink.admin.adapter.in.web.dto.SaveShopSettingsRequest;
import com.greenlink.core.application.shop.dto.ShopSettingsResult;
import com.greenlink.core.application.shop.usecase.ManageShopSettingsUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/shop/settings")
public class ShopSettingsController {

    private final ManageShopSettingsUseCase useCase;
    private final ShopAccess access;

    public ShopSettingsController(ManageShopSettingsUseCase useCase, ShopAccess access) {
        this.useCase = useCase;
        this.access = access;
    }

    @GetMapping
    public ResponseEntity<ShopSettingsResult> get(Authentication auth,
                                                  @RequestParam(required = false) Long companyId) {
        return ResponseEntity.ok(useCase.get(access.targetCompanyId(auth, companyId)));
    }

    @PostMapping
    public ResponseEntity<ShopSettingsResult> create(Authentication auth,
                                                     @RequestParam(required = false) Long companyId,
                                                     @RequestBody @Valid SaveShopSettingsRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(useCase.create(access.targetCompanyId(auth, companyId), req.toCommand()));
    }

    @PutMapping
    public ResponseEntity<ShopSettingsResult> update(Authentication auth,
                                                     @RequestParam(required = false) Long companyId,
                                                     @RequestBody @Valid SaveShopSettingsRequest req) {
        return ResponseEntity.ok(useCase.update(access.targetCompanyId(auth, companyId), req.toCommand()));
    }

    @PutMapping("/published")
    public ResponseEntity<ShopSettingsResult> setPublished(Authentication auth,
                                                           @RequestParam(required = false) Long companyId,
                                                           @RequestBody Map<String, Boolean> body) {
        boolean published = Boolean.TRUE.equals(body.get("published"));
        return ResponseEntity.ok(useCase.setPublished(access.targetCompanyId(auth, companyId), published));
    }

    @GetMapping("/slug-check")
    public ResponseEntity<Map<String, Boolean>> slugCheck(Authentication auth,
                                                          @RequestParam(required = false) Long companyId,
                                                          @RequestParam String slug) {
        return ResponseEntity.ok(Map.of("available",
                useCase.isSlugAvailable(access.targetCompanyId(auth, companyId), slug)));
    }
}
