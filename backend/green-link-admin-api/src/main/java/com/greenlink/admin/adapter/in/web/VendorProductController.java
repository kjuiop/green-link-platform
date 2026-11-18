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
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(manageVendorProductsUseCase.getProducts(companyId(auth), keyword,
                PageRequest.of(page, size, Sort.by("sortOrder").ascending().and(Sort.by("id").ascending()))));
    }

    @PostMapping
    public ResponseEntity<VendorProductResult> create(Authentication auth, @RequestBody @Valid ProductRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(manageVendorProductsUseCase.createProduct(companyId(auth), req.toCommand()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<VendorProductResult> update(Authentication auth, @PathVariable Long id,
                                                      @RequestBody @Valid ProductRequest req) {
        return ResponseEntity.ok(manageVendorProductsUseCase.updateProduct(companyId(auth), id, req.toCommand()));
    }

    @PatchMapping("/{id}/setting")
    public ResponseEntity<VendorProductResult> changeSetting(Authentication auth, @PathVariable Long id,
                                                             @RequestBody @Valid ProductSettingRequest req) {
        return ResponseEntity.ok(manageVendorProductsUseCase.changeSetting(companyId(auth), id, req.price(), req.enabled()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(Authentication auth, @PathVariable Long id) {
        manageVendorProductsUseCase.deleteProduct(companyId(auth), id);
        return ResponseEntity.noContent().build();
    }

    private Long companyId(Authentication auth) {
        if (auth != null && auth.getDetails() instanceof Long cid) {
            return cid;
        }
        throw new org.springframework.security.access.AccessDeniedException("회사 정보가 없습니다.");
    }
}
