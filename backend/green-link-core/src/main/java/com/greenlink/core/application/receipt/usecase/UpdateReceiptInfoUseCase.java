package com.greenlink.core.application.receipt.usecase;

import com.greenlink.core.application.receipt.dto.ReceiptConfirmationResult;
import com.greenlink.core.application.receipt.dto.UpdateReceiptInfoCommand;

public interface UpdateReceiptInfoUseCase {
    ReceiptConfirmationResult updateInfo(UpdateReceiptInfoCommand command);
}
