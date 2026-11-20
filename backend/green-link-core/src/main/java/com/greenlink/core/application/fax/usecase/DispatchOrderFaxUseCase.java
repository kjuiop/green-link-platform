package com.greenlink.core.application.fax.usecase;

import com.greenlink.core.application.fax.dto.FaxMessageResult;

public interface DispatchOrderFaxUseCase {

    void dispatchIfNeeded(Long orderId);

    FaxMessageResult resend(Long orderId);
}
