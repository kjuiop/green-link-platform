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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
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
            Authentication auth,
            @RequestParam(required = false) Long orderingCompanyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long resolvedCompanyId = resolveOrderingCompanyId(auth, orderingCompanyId);
        return ResponseEntity.ok(getProductsUseCase.getProducts(
                PageRequest.of(page, size, Sort.by("createdAt").descending()), resolvedCompanyId));
    }

    @GetMapping("/all")
    public ResponseEntity<List<ProductResult>> getAll(
            Authentication auth,
            @RequestParam(required = false) Long orderingCompanyId) {
        Long resolvedCompanyId = resolveOrderingCompanyId(auth, orderingCompanyId);
        return ResponseEntity.ok(getAllProductsUseCase.getAllProducts(resolvedCompanyId));
    }

    @PostMapping
    public ResponseEntity<ProductResult> create(
            Authentication auth,
            @RequestBody @Valid CreateProductRequest req) {
        Long resolvedCompanyId = resolveOrderingCompanyId(auth, req.orderingCompanyId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(createProductUseCase.createProduct(req.toCommand(resolvedCompanyId)));
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

    private Long resolveOrderingCompanyId(Authentication auth, Long requested) {
        if (auth instanceof UsernamePasswordAuthenticationToken token) {
            boolean isOrdering = token.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ORDERING"));
            if (isOrdering && token.getDetails() instanceof Long cid) {
                return cid;
            }
        }
        return requested;
    }
}
