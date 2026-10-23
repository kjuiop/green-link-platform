package com.greenlink.core.application.receipt.usecase;

import com.greenlink.core.application.receipt.dto.ReceiptConfirmationResult;
import com.greenlink.core.application.receipt.dto.SaveReceiptConfirmationCommand;

public interface SaveReceiptConfirmationUseCase {
    ReceiptConfirmationResult save(SaveReceiptConfirmationCommand command);
}
