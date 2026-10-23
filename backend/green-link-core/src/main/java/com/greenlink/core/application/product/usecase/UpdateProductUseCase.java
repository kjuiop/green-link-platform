package com.greenlink.core.application.product.usecase;

import com.greenlink.core.application.product.dto.UpdateProductCommand;
import com.greenlink.core.application.product.dto.ProductResult;

public interface UpdateProductUseCase {
    ProductResult updateProduct(Long id, UpdateProductCommand cmd);
}
