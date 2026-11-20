package com.greenlink.core.application.shop.service;

import com.greenlink.core.domain.shop.model.ShopOrder;
import com.greenlink.core.global.exception.BusinessException;
import com.greenlink.core.global.exception.shop.ShopErrorCode;

final class ShopOrderAccess {

    private ShopOrderAccess() {}

    /** callerCompanyId가 null이면 슈퍼관리자로 간주해 제한하지 않는다. */
    static void check(Long callerCompanyId, ShopOrder order) {
        if (callerCompanyId != null && !callerCompanyId.equals(order.getCompanyId())) {
            throw new BusinessException(ShopErrorCode.FORBIDDEN_COMPANY);
        }
    }
}
