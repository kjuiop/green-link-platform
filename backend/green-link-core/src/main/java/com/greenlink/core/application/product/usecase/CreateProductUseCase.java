package com.greenlink.core.application.product.usecase;

import com.greenlink.core.application.product.dto.CreateProductCommand;
import com.greenlink.core.application.product.dto.ProductResult;

public interface CreateProductUseCase {
    ProductResult createProduct(CreateProductCommand cmd);
}
