package com.greenlink.admin.adapter.in.event;

import com.greenlink.core.application.order.event.OrderCreatedEvent;
import com.greenlink.core.application.sms.usecase.SendOrderNotificationUseCase;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class OrderCreatedEventListener {

    private final SendOrderNotificationUseCase sendOrderNotificationUseCase;

    public OrderCreatedEventListener(SendOrderNotificationUseCase sendOrderNotificationUseCase) {
        this.sendOrderNotificationUseCase = sendOrderNotificationUseCase;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderCreated(OrderCreatedEvent event) {
        sendOrderNotificationUseCase.notify(event.orderId(), event.vendorPhone(), event.vendorName(), event.orderingCompanyName());
    }
}
