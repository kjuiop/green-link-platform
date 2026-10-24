package com.greenlink.core.application.sms.usecase;

public interface SendOrderNotificationUseCase {
    void notify(Long orderId, String vendorPhone);
}
