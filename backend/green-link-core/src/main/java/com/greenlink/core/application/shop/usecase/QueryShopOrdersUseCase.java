package com.greenlink.core.application.shop.usecase;

import com.greenlink.core.application.common.dto.PageResult;
import com.greenlink.core.application.shop.dto.ShopOrderResult;
import com.greenlink.core.domain.shop.model.ShopOrderStatus;

public interface QueryShopOrdersUseCase {

    /** callerCompanyId: 일반 계정이면 본인 회사 ID, 슈퍼관리자면 null(제한 없음). */
    PageResult<ShopOrderResult> list(Long callerCompanyId, Long filterCompanyId, ShopOrderStatus status,
                                     int page, int size);

    ShopOrderResult get(Long callerCompanyId, Long id);

    long pendingCount(Long callerCompanyId, Long filterCompanyId);
}
