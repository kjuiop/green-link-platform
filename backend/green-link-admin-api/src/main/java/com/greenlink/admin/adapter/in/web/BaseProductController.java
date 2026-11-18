package com.greenlink.admin.adapter.in.web;

import com.greenlink.admin.adapter.in.web.dto.ProductRequest;
import com.greenlink.core.application.common.dto.PageResult;
import com.greenlink.core.application.product.dto.BaseProductResult;
import com.greenlink.core.application.product.usecase.ManageBaseProductsUseCase;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/base-products")
public class BaseProductController {

    private final ManageBaseProductsUseCase manageBaseProductsUseCase;

    public BaseProductController(ManageBaseProductsUseCase manageBaseProductsUseCase) {
        this.manageBaseProductsUseCase = manageBaseProductsUseCase;
    }

    @GetMapping
    public ResponseEntity<PageResult<BaseProductResult>> getPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(manageBaseProductsUseCase.getBaseProducts(
                PageRequest.of(page, size, Sort.by("sortOrder").ascending().and(Sort.by("id").ascending()))));
    }

    @GetMapping("/all")
    public ResponseEntity<List<BaseProductResult>> getAll() {
        return ResponseEntity.ok(manageBaseProductsUseCase.getAllBaseProducts());
    }

    @PostMapping
    public ResponseEntity<BaseProductResult> create(@RequestBody @Valid ProductRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(manageBaseProductsUseCase.createBaseProduct(req.toCommand()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BaseProductResult> update(@PathVariable Long id,
                                                    @RequestBody @Valid ProductRequest req) {
        return ResponseEntity.ok(manageBaseProductsUseCase.updateBaseProduct(id, req.toCommand()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        manageBaseProductsUseCase.deleteBaseProduct(id);
        return ResponseEntity.noContent().build();
    }
}
