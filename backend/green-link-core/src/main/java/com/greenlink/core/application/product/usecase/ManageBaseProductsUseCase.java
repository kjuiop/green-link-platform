package com.greenlink.core.application.product.usecase;

import com.greenlink.core.application.common.dto.PageResult;
import com.greenlink.core.application.product.dto.BaseProductResult;
import com.greenlink.core.application.product.dto.ProductCommand;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ManageBaseProductsUseCase {
    PageResult<BaseProductResult> getBaseProducts(Pageable pageable);
    List<BaseProductResult> getAllBaseProducts();
    BaseProductResult createBaseProduct(ProductCommand cmd);
    BaseProductResult updateBaseProduct(Long id, ProductCommand cmd);
    void deleteBaseProduct(Long id);
}
