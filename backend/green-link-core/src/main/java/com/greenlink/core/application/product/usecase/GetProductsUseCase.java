package com.greenlink.core.application.product.usecase;

import com.greenlink.core.application.common.dto.PageResult;
import com.greenlink.core.application.product.dto.ProductResult;
import org.springframework.data.domain.Pageable;

public interface GetProductsUseCase {
    PageResult<ProductResult> getProducts(Pageable pageable);
}
