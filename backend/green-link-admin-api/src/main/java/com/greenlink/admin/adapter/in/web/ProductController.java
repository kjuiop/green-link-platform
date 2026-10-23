package com.greenlink.admin.adapter.in.web;

import com.greenlink.admin.adapter.in.web.dto.CreateProductRequest;
import com.greenlink.admin.adapter.in.web.dto.UpdateProductRequest;
import com.greenlink.core.application.common.dto.PageResult;
import com.greenlink.core.application.product.dto.ProductResult;
import com.greenlink.core.application.product.usecase.CreateProductUseCase;
import com.greenlink.core.application.product.usecase.DeleteProductUseCase;
import com.greenlink.core.application.product.usecase.GetAllProductsUseCase;
import com.greenlink.core.application.product.usecase.GetProductsUseCase;
import com.greenlink.core.application.product.usecase.UpdateProductUseCase;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final GetProductsUseCase getProductsUseCase;
    private final GetAllProductsUseCase getAllProductsUseCase;
    private final CreateProductUseCase createProductUseCase;
    private final UpdateProductUseCase updateProductUseCase;
    private final DeleteProductUseCase deleteProductUseCase;

    public ProductController(GetProductsUseCase getProductsUseCase,
                             GetAllProductsUseCase getAllProductsUseCase,
                             CreateProductUseCase createProductUseCase,
                             UpdateProductUseCase updateProductUseCase,
                             DeleteProductUseCase deleteProductUseCase) {
        this.getProductsUseCase = getProductsUseCase;
        this.getAllProductsUseCase = getAllProductsUseCase;
        this.createProductUseCase = createProductUseCase;
        this.updateProductUseCase = updateProductUseCase;
        this.deleteProductUseCase = deleteProductUseCase;
    }

    @GetMapping
    public ResponseEntity<PageResult<ProductResult>> getPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(getProductsUseCase.getProducts(
                PageRequest.of(page, size, Sort.by("createdAt").descending())));
    }

    @GetMapping("/all")
    public ResponseEntity<List<ProductResult>> getAll() {
        return ResponseEntity.ok(getAllProductsUseCase.getAllProducts());
    }

    @PostMapping
    public ResponseEntity<ProductResult> create(@RequestBody @Valid CreateProductRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(createProductUseCase.createProduct(req.toCommand()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResult> update(@PathVariable Long id,
                                                @RequestBody @Valid UpdateProductRequest req) {
        return ResponseEntity.ok(updateProductUseCase.updateProduct(id, req.toCommand()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        deleteProductUseCase.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
