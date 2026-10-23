package com.greenlink.core.application.receipt.usecase;

import com.greenlink.core.application.receipt.dto.ReceiptConfirmationResult;
import com.greenlink.core.application.receipt.dto.SaveReceiptConfirmationCommand.PhotoFile;

import java.util.List;

public interface AddReceiptPhotosUseCase {
    ReceiptConfirmationResult addPhotos(Long orderId, List<PhotoFile> photoFiles);
}
