package com.greenlink.core.application.customerorder.usecase;

import com.greenlink.core.application.customerorder.dto.CustomerOrderResult;

public interface ProcessCustomerOrderUseCase {

    CustomerOrderResult hold(Long callerCompanyId, Long id);

    CustomerOrderResult release(Long callerCompanyId, Long id);
}
