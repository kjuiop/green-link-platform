package com.greenlink.admin.adapter.in.web;

import com.greenlink.admin.adapter.in.web.dto.ProductRequest;
import com.greenlink.admin.adapter.in.web.dto.ProductSettingRequest;
import com.greenlink.core.application.common.dto.PageResult;
import com.greenlink.core.application.product.dto.VendorProductResult;
import com.greenlink.core.application.product.usecase.ManageVendorProductsUseCase;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/vendor/products")
public class VendorProductController {

    private final ManageVendorProductsUseCase manageVendorProductsUseCase;

    public VendorProductController(ManageVendorProductsUseCase manageVendorProductsUseCase) {
        this.manageVendorProductsUseCase = manageVendorProductsUseCase;
    }

    @GetMapping
    public ResponseEntity<PageResult<VendorProductResult>> getProducts(
            Authentication auth,
            @RequestParam(required = false) Long companyId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(manageVendorProductsUseCase.getProducts(resolveCompanyId(auth, companyId), keyword,
                PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100), Sort.by("sortOrder").ascending().and(Sort.by("id").ascending()))));
    }

    @PostMapping
    public ResponseEntity<VendorProductResult> create(Authentication auth,
                                                      @RequestParam(required = false) Long companyId,
                                                      @RequestBody @Valid ProductRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(manageVendorProductsUseCase.createProduct(resolveCompanyId(auth, companyId), req.toCommand()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<VendorProductResult> update(Authentication auth, @PathVariable Long id,
                                                      @RequestParam(required = false) Long companyId,
                                                      @RequestBody @Valid ProductRequest req) {
        return ResponseEntity.ok(manageVendorProductsUseCase.updateProduct(resolveCompanyId(auth, companyId), id, req.toCommand()));
    }

    @PatchMapping("/{id}/setting")
    public ResponseEntity<VendorProductResult> changeSetting(Authentication auth, @PathVariable Long id,
                                                             @RequestParam(required = false) Long companyId,
                                                             @RequestBody @Valid ProductSettingRequest req) {
        return ResponseEntity.ok(manageVendorProductsUseCase.changeSetting(resolveCompanyId(auth, companyId), id, req.price(), req.enabled()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(Authentication auth, @PathVariable Long id,
                                       @RequestParam(required = false) Long companyId) {
        manageVendorProductsUseCase.deleteProduct(resolveCompanyId(auth, companyId), id);
        return ResponseEntity.noContent().build();
    }

    private Long resolveCompanyId(Authentication auth, Long paramCompanyId) {
        boolean isSuperAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"));
        if (isSuperAdmin) {
            if (paramCompanyId == null) {
                throw new IllegalArgumentException("companyId는 필수입니다.");
            }
            return paramCompanyId;
        }
        if (auth.getDetails() instanceof Long cid) {
            return cid;
        }
        throw new org.springframework.security.access.AccessDeniedException("회사 정보가 없습니다.");
    }
}
