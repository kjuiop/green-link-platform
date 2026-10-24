package com.greenlink.core.application.receipt.dto;

import java.time.LocalDateTime;
import java.util.List;

public record SaveReceiptConfirmationCommand(
        Long orderId,
        String receiverName,
        String receiverRelation,
        boolean delivered,
        String memo,
        LocalDateTime confirmedAt,
        List<PhotoFile> photoFiles
) {
    public record PhotoFile(String originalFileName, byte[] content, String contentType, String photoType) {
        public PhotoFile(String originalFileName, byte[] content, String contentType) {
            this(originalFileName, content, contentType, "SITE");
        }
    }
}
