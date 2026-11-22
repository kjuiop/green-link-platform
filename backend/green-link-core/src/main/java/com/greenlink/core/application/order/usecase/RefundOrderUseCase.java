package com.greenlink.core.application.order.usecase;

import com.greenlink.core.application.order.dto.OrderResult;

public interface RefundOrderUseCase {

    OrderResult requestRefund(Long id, String reason, boolean proxy);

    OrderResult approveRefund(Long id, boolean proxy);

    OrderResult rejectRefund(Long id, String reason, boolean proxy);
}
