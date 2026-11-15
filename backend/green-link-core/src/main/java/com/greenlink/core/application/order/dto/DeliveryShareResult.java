package com.greenlink.core.application.order.dto;

import java.util.List;

public record DeliveryShareResult(
        String recipientName,
        String placeName,
        String completedAt,
        List<String> photoUrls
) {}
