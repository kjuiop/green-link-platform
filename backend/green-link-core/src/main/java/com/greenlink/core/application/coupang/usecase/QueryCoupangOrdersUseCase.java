package com.greenlink.core.application.coupang.usecase;

import com.greenlink.core.application.common.dto.PageResult;
import com.greenlink.core.application.coupang.dto.CoupangOrderResult;
import com.greenlink.core.domain.coupang.model.CoupangOrderStatus;

public interface QueryCoupangOrdersUseCase {

    /** callerCompanyId: 발주사 계정이면 본인 회사 ID, 슈퍼관리자면 null(제한 없음). */
    PageResult<CoupangOrderResult> list(Long callerCompanyId, Long filterCompanyId, CoupangOrderStatus status,
                                        int page, int size);

    CoupangOrderResult get(Long callerCompanyId, Long id);

    long pendingCount(Long callerCompanyId, Long filterCompanyId);
}
