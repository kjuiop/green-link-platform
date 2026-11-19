package com.greenlink.core.application.coupang.service;

import com.greenlink.core.domain.coupang.model.CoupangOrder;
import com.greenlink.core.global.exception.BusinessException;
import com.greenlink.core.global.exception.coupang.CoupangErrorCode;

final class CoupangOrderAccess {

    private CoupangOrderAccess() {}

    /** callerCompanyId가 null이면 슈퍼관리자로 간주해 제한하지 않는다. */
    static void check(Long callerCompanyId, CoupangOrder order) {
        if (callerCompanyId != null && !callerCompanyId.equals(order.getCompanyId())) {
            throw new BusinessException(CoupangErrorCode.FORBIDDEN_COMPANY);
        }
    }
}
