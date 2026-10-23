package com.greenlink.core.application.receipt.usecase;

import com.greenlink.core.application.receipt.dto.ReceiptConfirmationResult;

import java.util.Optional;

public interface GetReceiptConfirmationUseCase {
    Optional<ReceiptConfirmationResult> getByOrderId(Long orderId);
}
