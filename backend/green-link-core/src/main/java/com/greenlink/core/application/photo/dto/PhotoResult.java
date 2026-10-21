package com.greenlink.core.application.photo.dto;

public record PhotoResult(
        Long id,
        Long orderId,
        String photoUrl,
        String photoType,
        boolean isSendToCustomer,
        String uploadedAt
) {}
