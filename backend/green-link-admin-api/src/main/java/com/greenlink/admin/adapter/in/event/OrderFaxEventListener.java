package com.greenlink.admin.adapter.in.event;

import com.greenlink.core.application.fax.event.OrderFaxRequestedEvent;
import com.greenlink.core.application.fax.usecase.DispatchOrderFaxUseCase;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class OrderFaxEventListener {

    private final DispatchOrderFaxUseCase dispatchOrderFaxUseCase;

    public OrderFaxEventListener(DispatchOrderFaxUseCase dispatchOrderFaxUseCase) {
        this.dispatchOrderFaxUseCase = dispatchOrderFaxUseCase;
    }

    @Async("faxExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderFaxRequested(OrderFaxRequestedEvent event) {
        dispatchOrderFaxUseCase.dispatchIfNeeded(event.orderId());
    }
}
