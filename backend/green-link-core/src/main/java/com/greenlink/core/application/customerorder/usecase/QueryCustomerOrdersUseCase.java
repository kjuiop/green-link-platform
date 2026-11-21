package com.greenlink.core.application.customerorder.usecase;

import com.greenlink.core.application.common.dto.PageResult;
import com.greenlink.core.application.customerorder.dto.CustomerOrderResult;
import com.greenlink.core.domain.customerorder.model.CustomerOrderStatus;
import com.greenlink.core.domain.customerorder.model.OrderChannel;

public interface QueryCustomerOrdersUseCase {

    /** callerCompanyId: 발주사 계정이면 본인 회사 ID, 슈퍼관리자면 null(제한 없음). */
    PageResult<CustomerOrderResult> list(Long callerCompanyId, Long companyId, OrderChannel channel,
                                         CustomerOrderStatus status, int page, int size);

    CustomerOrderResult get(Long callerCompanyId, Long id);

    long pendingCount(Long callerCompanyId, Long companyId, OrderChannel channel);
}
