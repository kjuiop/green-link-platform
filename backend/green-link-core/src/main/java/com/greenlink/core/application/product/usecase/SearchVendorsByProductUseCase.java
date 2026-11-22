package com.greenlink.core.application.product.usecase;

import com.greenlink.core.application.product.dto.VendorSearchResult;
import com.greenlink.core.domain.product.model.ProductCategory;

import java.util.List;

public interface SearchVendorsByProductUseCase {
    List<VendorSearchResult> search(ProductCategory category, Long baseProductId, String keyword,
                                    String sido, String sigungu);
}
