package com.greenlink.core.application.sms.usecase;

import java.util.List;

public interface SendDeliveryMmsUseCase {
    void send(Long orderId, List<String> photoUrls);
}
