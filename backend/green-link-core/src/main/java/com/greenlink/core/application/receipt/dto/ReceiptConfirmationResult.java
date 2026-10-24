package com.greenlink.core.application.receipt.dto;

import java.util.List;

public record ReceiptConfirmationResult(
        Long id,
        Long orderId,
        String receiverName,
        String receiverRelation,
        boolean delivered,
        String memo,
        String confirmedAt,
        List<String> sitePhotoUrls,
        List<String> storePhotoUrls
) {}
