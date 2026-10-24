package com.greenlink.core.application.receipt.dto;

import java.time.LocalDateTime;

public record UpdateReceiptInfoCommand(
        Long orderId,
        String receiverName,
        String receiverRelation,
        boolean delivered,
        String memo,
        LocalDateTime confirmedAt
) {}
