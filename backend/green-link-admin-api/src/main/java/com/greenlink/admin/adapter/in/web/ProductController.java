package com.greenlink.admin.adapter.in.web;

import com.greenlink.core.application.common.dto.PageResult;
import com.greenlink.core.application.product.dto.BaseProductResult;
import com.greenlink.core.application.product.dto.VendorProductResult;
import com.greenlink.core.application.product.dto.VendorSearchResult;
import com.greenlink.core.application.product.usecase.ManageBaseProductsUseCase;
import com.greenlink.core.application.product.usecase.SearchAvailableProductsUseCase;
import com.greenlink.core.application.product.usecase.SearchVendorsByProductUseCase;
import com.greenlink.core.domain.product.model.ProductCategory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ManageBaseProductsUseCase manageBaseProductsUseCase;
    private final SearchAvailableProductsUseCase searchAvailableProductsUseCase;

    private final SearchVendorsByProductUseCase searchVendorsByProductUseCase;

    public ProductController(ManageBaseProductsUseCase manageBaseProductsUseCase,
                             SearchAvailableProductsUseCase searchAvailableProductsUseCase,
                             SearchVendorsByProductUseCase searchVendorsByProductUseCase) {
        this.manageBaseProductsUseCase = manageBaseProductsUseCase;
        this.searchAvailableProductsUseCase = searchAvailableProductsUseCase;
        this.searchVendorsByProductUseCase = searchVendorsByProductUseCase;
    }

    @GetMapping("/vendors")
    public ResponseEntity<List<VendorSearchResult>> searchVendors(
            @RequestParam(required = false) ProductCategory category,
            @RequestParam(required = false) Long baseProductId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String sido,
            @RequestParam(required = false) String sigungu) {
        return ResponseEntity.ok(searchVendorsByProductUseCase.search(category, baseProductId, keyword, sido, sigungu));
    }

    @GetMapping("/all")
    public ResponseEntity<List<BaseProductResult>> getAll() {
        return ResponseEntity.ok(manageBaseProductsUseCase.getAllBaseProducts());
    }

    @GetMapping("/search")
    public ResponseEntity<PageResult<VendorProductResult>> search(
            @RequestParam Long vendorId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(searchAvailableProductsUseCase.searchAvailable(vendorId, keyword,
                PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100), Sort.by("sortOrder").ascending().and(Sort.by("id").ascending()))));
    }
}
