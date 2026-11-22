package com.greenlink.core.application.product.usecase;

import com.greenlink.core.application.common.dto.PageResult;
import com.greenlink.core.application.product.dto.VendorProductResult;
import org.springframework.data.domain.Pageable;

public interface SearchAvailableProductsUseCase {
    PageResult<VendorProductResult> searchAvailable(Long vendorAdminId, String keyword, Pageable pageable);
}
