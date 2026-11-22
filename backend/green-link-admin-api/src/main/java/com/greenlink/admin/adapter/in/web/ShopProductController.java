package com.greenlink.admin.adapter.in.web;

import com.greenlink.admin.adapter.in.web.dto.SaveShopProductRequest;
import com.greenlink.core.application.shop.dto.ShopProductResult;
import com.greenlink.core.application.shop.usecase.ManageShopProductUseCase;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shop/products")
public class ShopProductController {

    private final ManageShopProductUseCase useCase;
    private final ShopAccess access;

    public ShopProductController(ManageShopProductUseCase useCase, ShopAccess access) {
        this.useCase = useCase;
        this.access = access;
    }

    @GetMapping
    public ResponseEntity<List<ShopProductResult>> list(Authentication auth,
                                                        @RequestParam(required = false) Long companyId) {
        return ResponseEntity.ok(useCase.list(access.targetCompanyId(auth, companyId)));
    }

    @PostMapping
    public ResponseEntity<ShopProductResult> create(Authentication auth,
                                                    @RequestParam(required = false) Long companyId,
                                                    @RequestBody @Valid SaveShopProductRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(useCase.create(access.targetCompanyId(auth, companyId), req.toCommand()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ShopProductResult> update(Authentication auth,
                                                    @RequestParam(required = false) Long companyId,
                                                    @PathVariable Long id,
                                                    @RequestBody @Valid SaveShopProductRequest req) {
        return ResponseEntity.ok(useCase.update(access.targetCompanyId(auth, companyId), id, req.toCommand()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(Authentication auth,
                                       @RequestParam(required = false) Long companyId,
                                       @PathVariable Long id) {
        useCase.delete(access.targetCompanyId(auth, companyId), id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/reorder")
    public ResponseEntity<Void> reorder(Authentication auth,
                                        @RequestParam(required = false) Long companyId,
                                        @RequestBody @NotEmpty List<Long> orderedIds) {
        useCase.reorder(access.targetCompanyId(auth, companyId), orderedIds);
        return ResponseEntity.noContent().build();
    }
}
