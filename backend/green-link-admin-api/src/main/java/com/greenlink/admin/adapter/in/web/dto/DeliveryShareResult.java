package com.greenlink.admin.adapter.in.web.dto;

import java.util.List;

public record DeliveryShareResult(
        String recipientName,
        String placeName,
        String completedAt,
        List<String> photoUrls
) {}
