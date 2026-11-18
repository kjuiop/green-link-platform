package com.greenlink.core.application.product.usecase;

import com.greenlink.core.application.common.dto.PageResult;
import com.greenlink.core.application.product.dto.ProductCommand;
import com.greenlink.core.application.product.dto.VendorProductResult;
import org.springframework.data.domain.Pageable;

public interface ManageVendorProductsUseCase {
    PageResult<VendorProductResult> getProducts(Long companyId, String keyword, Pageable pageable);
    VendorProductResult createProduct(Long companyId, ProductCommand cmd);
    VendorProductResult updateProduct(Long companyId, Long productId, ProductCommand cmd);
    void deleteProduct(Long companyId, Long productId);
    VendorProductResult changeSetting(Long companyId, Long productId, long price, boolean enabled);
}
