package com.greenlink.core.application.shop.usecase;

import com.greenlink.core.application.shop.dto.SaveShopProductCommand;
import com.greenlink.core.application.shop.dto.ShopProductResult;

import java.util.List;

public interface ManageShopProductUseCase {

    List<ShopProductResult> list(Long companyId);

    ShopProductResult create(Long companyId, SaveShopProductCommand command);

    ShopProductResult update(Long companyId, Long productId, SaveShopProductCommand command);

    void delete(Long companyId, Long productId);

    /** orderedIds 순서대로 sortOrder를 다시 매긴다. */
    void reorder(Long companyId, List<Long> orderedIds);
}
