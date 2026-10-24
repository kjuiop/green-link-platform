package com.greenlink.core.application.referencephoto.dto;

public record ReferencePhotoResult(
        Long id,
        Long orderId,
        String photoUrl,
        String uploadedAt
) {}
