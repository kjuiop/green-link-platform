package com.greenlink.core.application.product.usecase;

import com.greenlink.core.application.product.dto.ProductResult;

import java.util.List;

public interface GetAllProductsUseCase {
    List<ProductResult> getAllProducts(Long orderingCompanyId);
}
